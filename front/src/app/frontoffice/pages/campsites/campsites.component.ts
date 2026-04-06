// Module: Official Campsite & Booking | Layer: Frontend Component (Smart)
import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { CampsiteService } from '../../../services/campsite.service';
import { CampsiteApiResponse, CampsitePage } from '../../../models/campsite.model';

@Component({
  selector: 'app-campsites',
  templateUrl: './campsites.component.html',
  styleUrl: './campsites.component.css'
})
export class CampsitesComponent implements OnInit {

  campsites: CampsiteApiResponse[] = [];
  totalPages = 0;
  currentPage = 0;
  totalElements = 0;
  loading = false;
  error = '';

  // Filters
  searchCountry = '';
  searchCity = '';
  filterType: '' | 'OFFICIAL' | 'OUTDOOR' = '';
  minPrice: number | undefined;
  maxPrice: number | undefined;
  pageSize = 9;

  constructor(
    private campsiteService: CampsiteService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    this.loadCampsites();
  }

  loadCampsites(page = 0): void {
    this.loading = true;
    this.error = '';
    this.campsiteService.search({
      country:  this.searchCountry  || undefined,
      city:     this.searchCity     || undefined,
      type:     this.filterType     || undefined,
      minPrice: this.minPrice,
      maxPrice: this.maxPrice,
      page,
      size: this.pageSize
    }).subscribe({
      next: (data: CampsitePage) => {
        this.campsites    = data.content;
        this.totalPages   = data.totalPages;
        this.currentPage  = data.number;
        this.totalElements = data.totalElements;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load campsites. Please try again.';
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.loadCampsites(0);
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.loadCampsites(page);
    }
  }

  viewDetail(id: number): void {
    this.router.navigate(['/campsites', id]);
  }

  getFirstImage(campsite: CampsiteApiResponse): string {
    return campsite.pictures?.length ? campsite.pictures[0]
        : 'assets/images/campsite-placeholder.jpg';
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }
}
