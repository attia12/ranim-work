import { Component, OnInit } from '@angular/core';
import { EventService } from '../../../services/event.service';
import { AuthService } from '../../../services/auth.service';
import { Event, EventBooking } from '../../../models/event.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-bookings',
  templateUrl: './bookings.component.html',
  styleUrl: './bookings.component.css'
})
export class BookingsComponent implements OnInit {
  bookings: (EventBooking & { event?: Event })[] = [];
  loading: boolean = true;

  constructor(
    private eventService: EventService,
    private authService: AuthService
  ) { }

  ngOnInit() {
    const user = this.authService.getCurrentUser();
    if (user) {
      forkJoin({
        userBookings: this.eventService.getUserBookings(user.id),
        allEvents: this.eventService.getEvents()
      }).subscribe(({ userBookings, allEvents }) => {
        this.bookings = userBookings.map(booking => ({
          ...booking,
          event: allEvents.find(e => e.eventId === Number(booking.eventId))
        }));
        this.loading = false;
      });
    } else {
      this.loading = false;
    }
  }

  getStatusClass(status: string) {
    switch (status) {
      case 'CONFIRMED': return 'badge-success';
      case 'PENDING': return 'badge-warning';
      case 'CANCELLED': return 'badge-danger';
      default: return 'badge-secondary';
    }
  }
}
