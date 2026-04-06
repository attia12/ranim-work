import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Guide } from '../models/Guide.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class GuideService {
  private apiUrl = `${environment.apiUrl}/guides`;

  constructor(private http: HttpClient) { }

  getAll(): Observable<Guide[]> {
    return this.http.get<Guide[]>(this.apiUrl);
  }

  getById(id: number): Observable<Guide> {
    return this.http.get<Guide>(`${this.apiUrl}/${id}`);
  }

  create(data: Guide): Observable<Guide> {
    return this.http.post<Guide>(this.apiUrl, data);
  }

  update(id: number, data: Guide): Observable<Guide> {
    return this.http.put<Guide>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
