// Module: Official Campsite & Booking | Layer: Frontend Component (Smart)
import { Component, OnInit } from '@angular/core';
import { CampsiteBookingService } from '../../../services/campsite-booking.service';
import { OutdoorCampsiteService } from '../../../services/outdoor-campsite.service';
import { CampsiteBookingResponse, BookingPage } from '../../../models/campsite-booking.model';
import { OutdoorBookingResponse, OutdoorBookingPage } from '../../../models/outdoor-campsite.model';

@Component({
  selector: 'app-my-bookings',
  templateUrl: './my-bookings.component.html',
  styleUrl: './my-bookings.component.css'
})
export class MyBookingsComponent implements OnInit {

  officialBookings: CampsiteBookingResponse[] = [];
  outdoorCampsiteBookings: CampsiteBookingResponse[] = [];
  outdoorBookings: OutdoorBookingResponse[] = [];
  loading = false;
  cancellingId: number | null = null;
  cancelReason = '';
  showCancelModal = false;
  selectedBookingId: number | null = null;
  activeTab: 'official' | 'outdoor' = 'official';

  constructor(
    private bookingService: CampsiteBookingService,
    private outdoorService: OutdoorCampsiteService
  ) {}

  ngOnInit(): void {
    this.loadBookings();
  }

  loadBookings(): void {
    this.loading = true;
    this.bookingService.getMyBookings(0, 50).subscribe({
      next: (data: BookingPage) => {
        this.officialBookings = data.content.filter(b => b.campsiteType !== 'OUTDOOR');
        this.outdoorCampsiteBookings = data.content.filter(b => b.campsiteType === 'OUTDOOR');
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });

    this.outdoorService.getMyOutdoorBookings(0, 50).subscribe({
      next: (data: OutdoorBookingPage) => { this.outdoorBookings = data.content; },
      error: () => {}
    });
  }

  openCancelModal(id: number): void {
    this.selectedBookingId = id;
    this.cancelReason = '';
    this.showCancelModal = true;
  }

  confirmCancel(): void {
    if (!this.selectedBookingId) return;
    this.cancellingId = this.selectedBookingId;
    this.showCancelModal = false;
    this.bookingService.cancel(this.selectedBookingId, this.cancelReason).subscribe({
      next: (updated) => {
        const idx = this.officialBookings.findIndex(b => b.id === updated.id);
        if (idx >= 0) this.officialBookings[idx] = updated;
        this.cancellingId = null;
      },
      error: () => { this.cancellingId = null; }
    });
  }

  cancelOutdoor(id: number): void {
    this.outdoorService.cancelBooking(id).subscribe({
      next: (updated) => {
        const idx = this.outdoorBookings.findIndex(b => b.id === updated.id);
        if (idx >= 0) this.outdoorBookings[idx] = updated;
      }
    });
  }

  cancelOutdoorCampsite(id: number): void {
    this.bookingService.cancel(id, '').subscribe({
      next: (updated) => {
        const idx = this.outdoorCampsiteBookings.findIndex(b => b.id === updated.id);
        if (idx >= 0) this.outdoorCampsiteBookings[idx] = updated;
      }
    });
  }

  canCancel(checkInDate: string): boolean {
    const twoDaysFromNow = new Date();
    twoDaysFromNow.setDate(twoDaysFromNow.getDate() + 2);
    return new Date(checkInDate) > twoDaysFromNow;
  }

  badgeClass(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'badge-warning', CONFIRMED: 'badge-success',
      CANCELLED: 'badge-danger', COMPLETED: 'badge-secondary'
    };
    return map[status] || 'badge-light';
  }
}
