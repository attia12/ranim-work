// Module: Outdoor Campsite & Booking | Layer: Frontend Service
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ModerationRequest,
  OutdoorBookingPage,
  OutdoorBookingRequest,
  OutdoorBookingResponse,
  OutdoorCampsitePage,
  OutdoorCampsiteRequest,
  OutdoorCampsiteResponse
} from '../models/outdoor-campsite.model';

@Injectable({ providedIn: 'root' })
export class OutdoorCampsiteService {

  private sitesUrl    = `${environment.apiUrl}/api/v1/outdoor-campsites`;
  private bookingsUrl = `${environment.apiUrl}/api/v1/outdoor-bookings`;

  constructor(private http: HttpClient) {}

  // ── Outdoor Campsites ────────────────────────────────────────────────────

  /** Public: list approved outdoor campsites */
  getApproved(page = 0, size = 10): Observable<OutdoorCampsitePage> {
    const params = new HttpParams()
      .set('page', page).set('size', size).set('sort', 'createdAt,desc');
    return this.http.get<OutdoorCampsitePage>(this.sitesUrl, { params });
  }

  getById(id: number): Observable<OutdoorCampsiteResponse> {
    return this.http.get<OutdoorCampsiteResponse>(`${this.sitesUrl}/${id}`);
  }

  /** CAMPER: propose a new outdoor campsite */
  propose(request: OutdoorCampsiteRequest): Observable<OutdoorCampsiteResponse> {
    return this.http.post<OutdoorCampsiteResponse>(this.sitesUrl, request);
  }

  /** CAMPER: update own PENDING proposal */
  update(id: number, request: OutdoorCampsiteRequest): Observable<OutdoorCampsiteResponse> {
    return this.http.put<OutdoorCampsiteResponse>(`${this.sitesUrl}/${id}`, request);
  }

  /** ADMIN: list PENDING proposals */
  getPending(page = 0, size = 10): Observable<OutdoorCampsitePage> {
    const params = new HttpParams()
      .set('page', page).set('size', size).set('sort', 'createdAt,desc');
    return this.http.get<OutdoorCampsitePage>(`${this.sitesUrl}/pending`, { params });
  }

  /** ADMIN: approve/reject a proposal */
  moderate(id: number, request: ModerationRequest): Observable<OutdoorCampsiteResponse> {
    return this.http.patch<OutdoorCampsiteResponse>(`${this.sitesUrl}/${id}/moderate`, request);
  }

  /** CAMPER: get my proposals */
  getMyProposals(page = 0, size = 10): Observable<OutdoorCampsitePage> {
    const params = new HttpParams()
      .set('page', page).set('size', size).set('sort', 'createdAt,desc');
    return this.http.get<OutdoorCampsitePage>(`${this.sitesUrl}/my`, { params });
  }

  // ── Outdoor Bookings ─────────────────────────────────────────────────────

  /** Create free outdoor booking */
  book(request: OutdoorBookingRequest): Observable<OutdoorBookingResponse> {
    return this.http.post<OutdoorBookingResponse>(this.bookingsUrl, request);
  }

  getBookingById(id: number): Observable<OutdoorBookingResponse> {
    return this.http.get<OutdoorBookingResponse>(`${this.bookingsUrl}/${id}`);
  }

  getMyOutdoorBookings(page = 0, size = 10): Observable<OutdoorBookingPage> {
    const params = new HttpParams()
      .set('page', page).set('size', size).set('sort', 'createdAt,desc');
    return this.http.get<OutdoorBookingPage>(`${this.bookingsUrl}/my`, { params });
  }

  cancelBooking(id: number): Observable<OutdoorBookingResponse> {
    return this.http.patch<OutdoorBookingResponse>(`${this.bookingsUrl}/${id}/cancel`, {});
  }
}
