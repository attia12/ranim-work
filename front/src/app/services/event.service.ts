import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event, EventBooking } from '../models/event.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private apiUrl = `${environment.apiUrl}/events`;
  private bookingsApiUrl = `${environment.apiUrl}/bookings`;

  constructor(private http: HttpClient) { }

  getAll(): Observable<Event[]> {
    return this.http.get<Event[]>(this.apiUrl);
  }

  /** Alias used by BookingsComponent */
  getEvents(): Observable<Event[]> {
    return this.getAll();
  }

  getUserBookings(userId: number | string): Observable<EventBooking[]> {
    return this.http.get<EventBooking[]>(`${this.bookingsApiUrl}?userId=${userId}`);
  }

  getById(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrl}/${id}`);
  }

  create(data: Event): Observable<Event> {
    return this.http.post<Event>(this.apiUrl, data);
  }

  update(id: number, data: Event): Observable<Event> {
    return this.http.put<Event>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
