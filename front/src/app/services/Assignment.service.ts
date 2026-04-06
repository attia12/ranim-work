import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Assignment } from '../models/Assignment.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AssignmentService {
  private apiUrl = `${environment.apiUrl}/assignments`;

  constructor(private http: HttpClient) { }

  getAll(): Observable<Assignment[]> {
    return this.http.get<Assignment[]>(this.apiUrl);
  }

  getById(id: number): Observable<Assignment> {
    return this.http.get<Assignment>(`${this.apiUrl}/${id}`);
  }

  create(data: Assignment): Observable<Assignment> {
    return this.http.post<Assignment>(this.apiUrl, data);
  }

  update(id: number, data: Assignment): Observable<Assignment> {
    return this.http.put<Assignment>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
