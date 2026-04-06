// Module: Outdoor Campsite & Booking | Layer: Frontend Component (Smart)
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { OutdoorCampsiteService } from '../../../services/outdoor-campsite.service';
import { OutdoorCampsiteResponse } from '../../../models/outdoor-campsite.model';
import { AuthService } from '../../../services/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

interface OutdoorAvailability {
  id: number;
  startDate: string;
  endDate: string;
  isAvailable: boolean;
  note?: string;
  isFullyBooked?: boolean;
}

@Component({
  selector: 'app-outdoor-campsite-detail',
  templateUrl: './outdoor-campsite-detail.component.html',
  styleUrl: './outdoor-campsite-detail.component.css'
})
export class OutdoorCampsiteDetailComponent implements OnInit {

  site: OutdoorCampsiteResponse | null = null;
  availabilities: OutdoorAvailability[] = [];
  loading = true;
  error = '';
  activeImageIndex = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private outdoorService: OutdoorCampsiteService,
    private authService: AuthService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.outdoorService.getById(id).subscribe({
      next: (data) => {
        this.site = data;
        this.loading = false;
        this.loadAvailabilities(id);
      },
      error: () => {
        this.error = 'Outdoor campsite not found.';
        this.loading = false;
      }
    });
  }

  loadAvailabilities(siteId: number): void {
    this.http.get<OutdoorAvailability[]>(
      `${environment.apiUrl}/api/v1/outdoor-availabilities/site/${siteId}`
    ).subscribe({
      next: (data) => this.availabilities = data.filter(a => a.isAvailable && !a.isFullyBooked),
      error: () => {}
    });
  }

  bookNow(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.router.navigate(['/outdoor-campsites', this.site?.id, 'book']);
  }

  setActiveImage(i: number): void {
    this.activeImageIndex = i;
  }

  get activeImage(): string {
    if (!this.site?.pictures?.length) return 'assets/images/outdoor-placeholder.jpg';
    return this.site.pictures[this.activeImageIndex] || this.site.pictures[0];
  }

  difficultyBadge(diff: string | undefined): string {
    const map: Record<string, string> = { EASY: 'badge-success', MODERATE: 'badge-warning', HARD: 'badge-danger' };
    return map[diff || ''] || 'badge-secondary';
  }
}
