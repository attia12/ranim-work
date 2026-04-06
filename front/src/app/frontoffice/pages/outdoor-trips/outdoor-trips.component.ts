// Module: Outdoor Campsite & Booking | Layer: Frontend Component (Smart)
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OutdoorCampsiteService } from '../../../services/outdoor-campsite.service';
import { OutdoorCampsiteResponse, OutdoorCampsitePage } from '../../../models/outdoor-campsite.model';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-outdoor-trips',
  templateUrl: './outdoor-trips.component.html',
  styleUrl: './outdoor-trips.component.css'
})
export class OutdoorTripsComponent implements OnInit {

  sites: OutdoorCampsiteResponse[] = [];
  totalPages = 0;
  currentPage = 0;
  totalElements = 0;
  loading = false;
  error = '';
  pageSize = 9;

  constructor(
    private outdoorService: OutdoorCampsiteService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadSites();
  }

  loadSites(page = 0): void {
    this.loading = true;
    this.error = '';
    this.outdoorService.getApproved(page, this.pageSize).subscribe({
      next: (data: OutdoorCampsitePage) => {
        this.sites         = data.content;
        this.totalPages    = data.totalPages;
        this.currentPage   = data.number;
        this.totalElements = data.totalElements;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load outdoor campsites.';
        this.loading = false;
      }
    });
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) this.loadSites(page);
  }

  viewDetail(id: number): void {
    this.router.navigate(['/outdoor-campsites', id]);
  }

  proposeNew(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.router.navigate(['/propose-outdoor']);
  }

  difficultyBadge(diff: string | undefined): string {
    const map: Record<string, string> = { EASY: 'badge-success', MODERATE: 'badge-warning', HARD: 'badge-danger' };
    return map[diff || ''] || 'badge-secondary';
  }

  getFirstImage(site: OutdoorCampsiteResponse): string {
    return site.pictures?.length ? site.pictures[0] : 'assets/images/outdoor-placeholder.jpg';
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }
}
