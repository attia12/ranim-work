// Module: Outdoor Campsite & Booking | Layer: Frontend Component (Smart)
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { OutdoorCampsiteService } from '../../../services/outdoor-campsite.service';
import { OutdoorCampsiteResponse } from '../../../models/outdoor-campsite.model';

@Component({
  selector: 'app-outdoor-booking',
  templateUrl: './outdoor-booking.component.html',
  styleUrl: './outdoor-booking.component.css'
})
export class OutdoorBookingComponent implements OnInit {

  bookingForm!: FormGroup;
  site: OutdoorCampsiteResponse | null = null;
  loading = false;
  submitting = false;
  error = '';
  success = '';
  today = new Date().toISOString().split('T')[0];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private outdoorService: OutdoorCampsiteService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loading = true;
    this.outdoorService.getById(id).subscribe({
      next: (data) => { this.site = data; this.loading = false; },
      error: () => { this.error = 'Outdoor campsite not found.'; this.loading = false; }
    });

    this.bookingForm = this.fb.group({
      checkInDate:    ['', [Validators.required]],
      checkOutDate:   ['', [Validators.required]],
      numberOfGuests: [1, [Validators.required, Validators.min(1), Validators.max(50)]]
    });
  }

  get nights(): number {
    const checkIn  = this.bookingForm.get('checkInDate')?.value;
    const checkOut = this.bookingForm.get('checkOutDate')?.value;
    if (!checkIn || !checkOut) return 0;
    const diff = new Date(checkOut).getTime() - new Date(checkIn).getTime();
    return Math.max(0, Math.floor(diff / (1000 * 60 * 60 * 24)));
  }

  submit(): void {
    if (this.bookingForm.invalid || !this.site) return;
    this.submitting = true;
    this.error = '';

    this.outdoorService.book({
      outdoorCampsiteId: this.site.id,
      checkInDate:       this.bookingForm.value.checkInDate,
      checkOutDate:      this.bookingForm.value.checkOutDate,
      numberOfGuests:    this.bookingForm.value.numberOfGuests
    }).subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/my-bookings']);
      },
      error: (err) => {
        this.submitting = false;
        this.error = err.error?.message || err.error?.error || 'Booking failed. Please try again.';
      }
    });
  }
}
