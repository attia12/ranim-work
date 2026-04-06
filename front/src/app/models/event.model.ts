export interface Event {
  id?: number;
  eventId: number;
  title: string;
  location: string;
  date: string;
  maxParticipants: number;
  status: string;
  imageUrl?: string;
  category?: string;
  price?: number;
  event_organizer_id?: number;   // required by backend @NotNull
  organizerFullName?: string;    // returned in EventResponse
}

export interface EventBooking {
  id: string;
  eventId: string;
  userId: string;
  bookingDate: Date;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
}
