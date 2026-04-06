// Module: Official Campsite & Booking | Layer: Frontend Component (Smart)
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CampsiteService } from '../../../services/campsite.service';
import { CampsiteApiResponse } from '../../../models/campsite.model';
import { AvailabilityResponse } from '../../../models/availability.model';
import { AuthService } from '../../../services/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-campsite-detail',
  templateUrl: './campsite-detail.component.html',
  styleUrl: './campsite-detail.component.css'
})
export class CampsiteDetailComponent implements OnInit {

  campsite: CampsiteApiResponse | null = null;
  availabilities: AvailabilityResponse[] = [];
  loading = true;
  error = '';
  activeImageIndex = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private campsiteService: CampsiteService,
    private authService: AuthService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.campsiteService.getCampsiteById(id).subscribe({
      next: (data) => {
        this.campsite = data;
        this.loading = false;
        this.loadAvailability(id);
      },
      error: () => {
        this.error = 'Campsite not found.';
        this.loading = false;
      }
    });
  }

  loadAvailability(campsiteId: number): void {
    this.http.get<AvailabilityResponse[]>(
      `${environment.apiUrl}/api/v1/availabilities/campsite/${campsiteId}`
    ).subscribe({
      next: (data) => this.availabilities = data,
      error: () => {}
    });
  }

  bookNow(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.router.navigate(['/campsites', this.campsite?.id, 'book']);
  }

  setActiveImage(i: number): void {
    this.activeImageIndex = i;
  }

  get activeImage(): string {
    if (!this.campsite?.pictures?.length) return 'assets/images/campsite-placeholder.jpg';
    return this.campsite.pictures[this.activeImageIndex] || this.campsite.pictures[0];
  }
}
