// Module: Official Campsite & Booking | Layer: Frontend Component (Smart)
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CampsiteService } from '../../../services/campsite.service';
import { CampsiteBookingService } from '../../../services/campsite-booking.service';
import { CampsiteApiResponse } from '../../../models/campsite.model';

@Component({
  selector: 'app-campsite-booking',
  templateUrl: './campsite-booking.component.html',
  styleUrl: './campsite-booking.component.css'
})
export class CampsiteBookingComponent implements OnInit {

  bookingForm!: FormGroup;
  campsite: CampsiteApiResponse | null = null;
  loading = false;
  submitting = false;
  error = '';
  today = new Date().toISOString().split('T')[0];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private campsiteService: CampsiteService,
    private bookingService: CampsiteBookingService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loading = true;
    this.campsiteService.getCampsiteById(id).subscribe({
      next: (data) => { this.campsite = data; this.loading = false; },
      error: () => { this.error = 'Campsite not found.'; this.loading = false; }
    });

    this.bookingForm = this.fb.group({
      checkInDate:    ['', [Validators.required]],
      checkOutDate:   ['', [Validators.required]],
      numberOfGuests: [1, [Validators.required, Validators.min(1)]]
    });
  }

  get nights(): number {
    const checkIn  = this.bookingForm.get('checkInDate')?.value;
    const checkOut = this.bookingForm.get('checkOutDate')?.value;
    if (!checkIn || !checkOut) return 0;
    const diff = new Date(checkOut).getTime() - new Date(checkIn).getTime();
    return Math.max(0, Math.floor(diff / (1000 * 60 * 60 * 24)));
  }

  get totalPrice(): number {
    return this.nights * (this.campsite?.pricePerNight ?? 0);
  }

  submit(): void {
    if (this.bookingForm.invalid || !this.campsite) return;
    this.submitting = true;
    this.error = '';

    this.bookingService.create({
      campsiteId:     this.campsite.id,
      checkInDate:    this.bookingForm.value.checkInDate,
      checkOutDate:   this.bookingForm.value.checkOutDate,
      numberOfGuests: this.bookingForm.value.numberOfGuests
    }).subscribe({
      next: (booking) => {
        this.submitting = false;
        // Navigate to payment stub page
        this.router.navigate(['/campsite-payment'], { queryParams: { bookingId: booking.id, amount: booking.totalPrice } });
      },
      error: (err) => {
        this.submitting = false;
        this.error = err.error?.message || err.error?.error || 'Booking failed. Please try again.';
      }
    });
  }
}
