import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-events',
  templateUrl: './events.component.html',
  styleUrl: './events.component.css'
})
export class EventsComponent implements OnInit {

  selectedStatus: string = 'All';
  statuses: string[] = ['All', 'ACTIVE', 'INACTIVE', 'CANCELLED'];
  selectedEvent: any = null;

  showForm = false;
  isEditing = false;

  formData: any = {
    eventId: 0, title: '', location: '', date: '', maxParticipants: 0, status: 'ACTIVE'
  };

  events: any[] = [
    {
      eventId: 1,
      title: 'Summer Solstice Wild Camp',
      location: 'Rocky Mountains, CO',
      date: '2026-06-20',
      maxParticipants: 80,
      status: 'ACTIVE',
      image: 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=600',
      description: 'Join us for a breathtaking weekend of wild camping and stargazing deep in the heart of the Rockies. Perfect for nature lovers and adventure seekers.',
      guides: [
        { name: 'James Carter', experience: 8, role: 'Lead Survival Guide' },
        { name: 'Sofia Moreau', experience: 5, role: 'Navigation Specialist' }
      ],
      highlights: ['Stargazing session', 'Survival skills workshop', 'Campfire dinner']
    },
    {
      eventId: 2,
      title: 'Alpine Summit Expedition',
      location: 'Sierra Nevada, CA',
      date: '2026-08-10',
      maxParticipants: 30,
      status: 'ACTIVE',
      image: 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600',
      description: 'A challenging 3-day trek to the summit of Mount Whitney. Only for experienced hikers ready to push their limits.',
      guides: [
        { name: 'Lucas Bennett', experience: 12, role: 'Mountain Guide' }
      ],
      highlights: ['Summit attempt', 'High altitude camping', 'Professional gear provided']
    },
    {
      eventId: 3,
      title: 'Lakeside Yoga Retreat',
      location: 'Lake Tahoe, CA',
      date: '2026-07-15',
      maxParticipants: 50,
      status: 'ACTIVE',
      image: 'https://images.unsplash.com/photo-1506484334402-40ff22e05a6d?w=600',
      description: 'Morning yoga sessions, afternoon paddleboarding, and campfire meditation by the lake. A perfect blend of wellness and outdoor adventure.',
      guides: [
        { name: 'Amina Youssef', experience: 6, role: 'Wellness & Yoga Guide' },
        { name: 'Tom Rivera', experience: 4, role: 'Water Sports Instructor' }
      ],
      highlights: ['Daily yoga sessions', 'Paddleboarding', 'Meditation by the lake']
    },
    {
      eventId: 4,
      title: 'Desert Night Safari',
      location: 'Sahara Desert, Tunisia',
      date: '2026-09-05',
      maxParticipants: 25,
      status: 'INACTIVE',
      image: 'https://images.unsplash.com/photo-1496080174650-637e3f22fa03?w=600',
      description: 'Experience the magic of the Sahara under a blanket of stars. Camel rides, traditional meals, and guided night walks through the dunes.',
      guides: [
        { name: 'Karim Slama', experience: 10, role: 'Desert Expedition Guide' }
      ],
      highlights: ['Camel ride at sunset', 'Traditional Berber dinner', 'Night dune walk']
    }
  ];

  filteredEvents: any[] = [];

  constructor(private authService: AuthService) {}

  ngOnInit() { this.filterEvents(); }

  isAdmin(): boolean {
    const user = this.authService.getCurrentUser();
    return user?.role === 'ADMIN';
  }

  filterEvents() {
    this.filteredEvents = this.selectedStatus === 'All'
      ? this.events
      : this.events.filter(e => e.status === this.selectedStatus);
  }

  selectStatus(status: string) {
    this.selectedStatus = status;
    this.filterEvents();
  }

  openDetails(event: any) { this.selectedEvent = event; }
  closeDetails() { this.selectedEvent = null; }

  openForm() {
    this.showForm = true; this.isEditing = false;
    this.formData = { eventId: 0, title: '', location: '', date: '', maxParticipants: 0, status: 'ACTIVE' };
  }

  editEvent(event: any, $event: MouseEvent) {
    $event.stopPropagation();
    this.isEditing = true; this.showForm = true;
    this.formData = { ...event };
  }

  saveEvent() {
    if (!this.formData.title || !this.formData.location || !this.formData.date) {
      alert('Please fill in all required fields.'); return;
    }
    if (this.isEditing) {
      const idx = this.events.findIndex(e => e.eventId === this.formData.eventId);
      if (idx > -1) this.events[idx] = { ...this.events[idx], ...this.formData };
    } else {
      this.formData.eventId = Date.now();
      this.formData.image = 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=600';
      this.formData.guides = [];
      this.formData.highlights = [];
      this.events.push({ ...this.formData });
    }
    this.filterEvents();
    this.closeForm();
  }

  deleteEvent(eventId: number, $event: MouseEvent) {
    $event.stopPropagation();
    if (confirm('Delete this event?')) {
      this.events = this.events.filter(e => e.eventId !== eventId);
      this.filterEvents();
    }
  }

  closeForm() { this.showForm = false; this.isEditing = false; }
}
