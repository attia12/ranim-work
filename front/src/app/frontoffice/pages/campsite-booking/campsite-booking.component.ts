// Module: Official Campsite & Booking | Layer: Frontend Component (Smart)
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CampsiteService } from '../../../services/campsite.service';
import { CampsiteBookingService } from '../../../services/campsite-booking.service';
import { CampsiteApiResponse } from '../../../models/campsite.model';
import { AvailabilityResponse } from '../../../models/availability.model';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-campsite-booking',
  templateUrl: './campsite-booking.component.html',
  styleUrl: './campsite-booking.component.css'
})
export class CampsiteBookingComponent implements OnInit {

  bookingForm!: FormGroup;
  campsite: CampsiteApiResponse | null = null;
  availabilities: AvailabilityResponse[] = [];
  loading = false;
  submitting = false;
  error = '';
  today = new Date().toISOString().split('T')[0];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private campsiteService: CampsiteService,
    private bookingService: CampsiteBookingService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loading = true;
    this.campsiteService.getCampsiteById(id).subscribe({
      next: (data) => {
        this.campsite = data;
        this.loading = false;
        this.http.get<AvailabilityResponse[]>(
          `${environment.apiUrl}/api/v1/availabilities/campsite/${id}`
        ).subscribe({ next: (av) => this.availabilities = av, error: () => {} });
      },
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

  get guests(): number {
    return Number(this.bookingForm.get('numberOfGuests')?.value) || 1;
  }

  get isOutdoor(): boolean {
    return this.campsite?.type === 'OUTDOOR';
  }

  get totalPrice(): number {
    if (this.isOutdoor) return 0;
    return this.nights * (this.campsite?.pricePerNight ?? 0) * this.guests;
  }

  get availabilityError(): string {
    const checkIn  = this.bookingForm.get('checkInDate')?.value;
    const checkOut = this.bookingForm.get('checkOutDate')?.value;
    if (!checkIn || !checkOut || this.nights <= 0 || this.availabilities.length === 0) return '';

    const start = new Date(checkIn);
    const end   = new Date(checkOut);

    // If any blocked window overlaps the requested range → blocked
    const blocked = this.availabilities.find(a =>
      a.isBlocked &&
      new Date(a.startDate) <= end &&
      new Date(a.endDate)   >= start
    );
    if (blocked) return 'These dates are blocked by the campsite owner. Please choose different dates.';

    // An open window must fully cover the requested range
    const covering = this.availabilities.find(a =>
      !a.isBlocked &&
      new Date(a.startDate) <= start &&
      new Date(a.endDate)   >= end
    );
    if (!covering) return 'No availability set for these dates. Please choose dates within an available period.';

    // Enough places for the number of guests
    if (covering.numberOfPlaces < this.guests) {
      return `Not enough spots. Only ${covering.numberOfPlaces} place(s) available for these dates.`;
    }

    return '';
  }

  submit(): void {
    if (this.bookingForm.invalid || !this.campsite || this.availabilityError) return;
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
        if (this.isOutdoor) {
          this.router.navigate(['/my-bookings']);
        } else {
          this.router.navigate(['/campsite-payment'], { queryParams: { bookingId: booking.id, amount: booking.totalPrice } });
        }
      },
      error: (err) => {
        this.submitting = false;
        this.error = err.error?.message || err.error?.error || 'Booking failed. Please try again.';
      }
    });
  }
}
