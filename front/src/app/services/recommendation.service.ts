import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, EMPTY } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { RecommendedCampsite } from '../models/recommended-campsite.model';

@Injectable({ providedIn: 'root' })
export class RecommendationService {

  private readonly apiUrl = `${environment.apiUrl}/api/v1/campsites/recommended`;

  constructor(private http: HttpClient) {}

  getRecommendations(): Observable<RecommendedCampsite[]> {
    return this.http.get<RecommendedCampsite[]>(this.apiUrl).pipe(
      catchError(err => {
        // 401/403 = not logged in, 503 = AI service down — both show fallback
        console.warn('[RecommendationService] Could not load recommendations:', err.status);
        return EMPTY;
      })
    );
  }
}
