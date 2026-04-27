// Module: Official Campsite & Booking | Layer: Frontend Service
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  BookingPage,
  CampsiteBookingRequest,
  CampsiteBookingResponse,
  CampsitePaymentRequest,
  CampsitePaymentResponse
} from '../models/campsite-booking.model';

@Injectable({ providedIn: 'root' })
export class CampsiteBookingService {

  private bookingsUrl = `${environment.apiUrl}/api/v1/campsite-bookings`;
  private paymentsUrl = `${environment.apiUrl}/api/v1/campsite-payments`;

  constructor(private http: HttpClient) {}

  /** Create a campsite booking */
  create(request: CampsiteBookingRequest): Observable<CampsiteBookingResponse> {
    return this.http.post<CampsiteBookingResponse>(this.bookingsUrl, request);
  }

  getById(id: number): Observable<CampsiteBookingResponse> {
    return this.http.get<CampsiteBookingResponse>(`${this.bookingsUrl}/${id}`);
  }

  /** Get MY bookings (camper view) */
  getMyBookings(page = 0, size = 10): Observable<BookingPage> {
    const params = new HttpParams()
      .set('page', page).set('size', size).set('sort', 'createdAt,desc');
    return this.http.get<BookingPage>(`${this.bookingsUrl}/my`, { params });
  }

  /** OWNER/ADMIN: bookings for a specific campsite */
  getByCampsite(campsiteId: number, page = 0, size = 10): Observable<BookingPage> {
    const params = new HttpParams()
      .set('page', page).set('size', size).set('sort', 'createdAt,desc');
    return this.http.get<BookingPage>(`${this.bookingsUrl}/campsite/${campsiteId}`, { params });
  }

  /** ADMIN: all bookings */
  getAll(page = 0, size = 10): Observable<BookingPage> {
    const params = new HttpParams()
      .set('page', page).set('size', size).set('sort', 'createdAt,desc');
    return this.http.get<BookingPage>(this.bookingsUrl, { params });
  }

  /** Cancel a booking */
  cancel(id: number, reason?: string): Observable<CampsiteBookingResponse> {
    return this.http.patch<CampsiteBookingResponse>(
      `${this.bookingsUrl}/${id}/cancel`,
      reason ? { reason } : {}
    );
  }

  /** OWNER/ADMIN: confirm a booking */
  confirm(id: number): Observable<CampsiteBookingResponse> {
    return this.http.patch<CampsiteBookingResponse>(`${this.bookingsUrl}/${id}/confirm`, {});
  }

  /** Stripe: create a PaymentIntent and get clientSecret */
  createPaymentIntent(bookingId: number, amount: number): Observable<{ clientSecret: string; publishableKey: string; paymentIntentId: string }> {
    return this.http.post<{ clientSecret: string; publishableKey: string; paymentIntentId: string }>(
      `${environment.apiUrl}/api/v1/stripe/payment-intent`,
      { bookingId, amount }
    );
  }

  /** Payment: record a completed payment */
  pay(request: CampsitePaymentRequest): Observable<CampsitePaymentResponse> {
    return this.http.post<CampsitePaymentResponse>(this.paymentsUrl, request);
  }

  getPaymentByBooking(bookingId: number): Observable<CampsitePaymentResponse> {
    return this.http.get<CampsitePaymentResponse>(`${this.paymentsUrl}/booking/${bookingId}`);
  }
}
