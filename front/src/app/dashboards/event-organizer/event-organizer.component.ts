import { Component, OnInit } from '@angular/core';
import { EventService } from '../../services/event.service';
import { AuthService } from '../../services/auth.service';
import { Event } from '../../models/event.model';

@Component({
  selector: 'app-event-organizer',
  templateUrl: './event-organizer.component.html',
  styleUrl: './event-organizer.component.css'
})
export class EventOrganizerComponent implements OnInit {
  events: Event[] = [];
  showModal = false;
  isEditing = false;
  loading = false;
  currentEventId: number | null = null;

  formData: Partial<Event> = this.emptyForm();

  constructor(
    private eventService: EventService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.loadEvents();
  }

  loadEvents() {
    this.loading = true;
    this.eventService.getAll().subscribe({
      next: (data) => { this.events = data; this.loading = false; },
      error: (err) => { console.error('Error loading events:', err); this.loading = false; }
    });
  }

  openAddModal() {
    this.isEditing = false;
    this.currentEventId = null;
    this.formData = this.emptyForm();
    this.showModal = true;
  }

  openEditModal(event: Event) {
    this.isEditing = true;
    this.currentEventId = event.eventId;
    this.formData = { ...event };
    this.showModal = true;
  }

  saveEvent() {
    if (!this.formData.title || !this.formData.date || !this.formData.location) {
      alert('Please fill in all required fields.');
      return;
    }

    // Attach the logged-in user's id as event_organizer_id (required by backend)
    const currentUser = this.authService.getCurrentUser();
    const organizerId = currentUser ? Number(currentUser.id) : null;

    if (!organizerId) {
      alert('Unable to determine organizer. Please log in again.');
      return;
    }

    const payload: Event = {
      ...(this.formData as Event),
      event_organizer_id: organizerId,
      maxParticipants: this.formData.maxParticipants ?? 1,
      price: this.formData.price ?? 0,
      status: this.formData.status || 'UPCOMING'
    };

    if (this.isEditing && this.currentEventId) {
      this.eventService.update(this.currentEventId, payload).subscribe({
        next: () => { this.closeModal(); this.loadEvents(); },
        error: (err) => { console.error('Error updating event:', err); alert('Failed to update event.'); }
      });
    } else {
      this.eventService.create(payload).subscribe({
        next: () => { this.closeModal(); this.loadEvents(); },
        error: (err) => { console.error('Error creating event:', err); alert('Failed to create event.'); }
      });
    }
  }

  deleteEvent(id: number) {
    if (confirm('Delete this event?')) {
      this.eventService.delete(id).subscribe({
        next: () => this.loadEvents(),
        error: (err) => { console.error('Error deleting event:', err); alert('Failed to delete event.'); }
      });
    }
  }

  closeModal() {
    this.showModal = false;
  }

  private emptyForm(): Partial<Event> {
    return { eventId: 0, title: '', location: '', date: '', maxParticipants: 1, status: 'UPCOMING', price: 0 };
  }
}
