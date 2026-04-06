// Module: Official Campsite & Booking | Layer: Frontend Service
// Updated: replaced mock data with real API calls to /api/v1/campsites
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CampsiteApiResponse,
  CampsiteRequest,
  CampsitePage
} from '../models/campsite.model';

@Injectable({ providedIn: 'root' })
export class CampsiteService {

  private apiUrl = `${environment.apiUrl}/api/v1/campsites`;

  constructor(private http: HttpClient) {}

  /** Public: search / list campsites with optional filters */
  search(filters: {
    country?: string;
    city?: string;
    type?: string;
    minPrice?: number;
    maxPrice?: number;
    page?: number;
    size?: number;
  }): Observable<CampsitePage> {
    let params = new HttpParams();
    if (filters.country)  params = params.set('country',  filters.country);
    if (filters.city)     params = params.set('city',     filters.city);
    if (filters.type)     params = params.set('type',     filters.type);
    if (filters.minPrice !== undefined) params = params.set('minPrice', filters.minPrice);
    if (filters.maxPrice !== undefined) params = params.set('maxPrice', filters.maxPrice);
    params = params.set('page', filters.page ?? 0);
    params = params.set('size', filters.size ?? 10);
    params = params.set('sort', 'createdAt,desc');
    return this.http.get<CampsitePage>(this.apiUrl, { params });
  }

  /** Public: get campsite by id */
  getCampsiteById(id: number): Observable<CampsiteApiResponse> {
    return this.http.get<CampsiteApiResponse>(`${this.apiUrl}/${id}`);
  }

  /** OWNER/ADMIN: get my campsites */
  getMyCampsites(page = 0, size = 10): Observable<CampsitePage> {
    const params = new HttpParams()
      .set('page', page).set('size', size).set('sort', 'createdAt,desc');
    return this.http.get<CampsitePage>(`${this.apiUrl}/my`, { params });
  }

  /** ADMIN: get all campsites */
  getAllAdmin(page = 0, size = 10): Observable<CampsitePage> {
    const params = new HttpParams()
      .set('page', page).set('size', size).set('sort', 'createdAt,desc');
    return this.http.get<CampsitePage>(`${this.apiUrl}/all`, { params });
  }

  /** OWNER/ADMIN: create campsite */
  create(request: CampsiteRequest): Observable<CampsiteApiResponse> {
    return this.http.post<CampsiteApiResponse>(this.apiUrl, request);
  }

  /** OWNER/ADMIN: update campsite */
  update(id: number, request: CampsiteRequest): Observable<CampsiteApiResponse> {
    return this.http.put<CampsiteApiResponse>(`${this.apiUrl}/${id}`, request);
  }

  /** OWNER/ADMIN: delete campsite */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /** ADMIN: suspend campsite */
  suspend(id: number): Observable<CampsiteApiResponse> {
    return this.http.patch<CampsiteApiResponse>(`${this.apiUrl}/${id}/suspend`, {});
  }

  /** ADMIN: activate campsite */
  activate(id: number): Observable<CampsiteApiResponse> {
    return this.http.patch<CampsiteApiResponse>(`${this.apiUrl}/${id}/activate`, {});
  }
}
