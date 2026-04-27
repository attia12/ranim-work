import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-campsite-analytics',
  templateUrl: './campsite-analytics.component.html',
  styleUrl: './campsite-analytics.component.css'
})
export class CampsiteAnalyticsComponent implements OnInit {

  loading = true;

  overview: any = {};
  revenueData: any[] = [];
  occupancyData: any[] = [];
  fraudSuspects: any[] = [];

  maxRevenue = 0;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    const base = `${environment.apiUrl}/api/v1/analytics`;

    this.http.get<any>(`${base}/overview`).subscribe({
      next: d => this.overview = d,
      error: () => {}
    });

    this.http.get<any[]>(`${base}/revenue-by-month`).subscribe({
      next: d => {
        this.revenueData = d;
        this.maxRevenue = Math.max(...d.map((r: any) => Number(r.revenue) || 0), 1);
      },
      error: () => {}
    });

    this.http.get<any[]>(`${base}/occupancy`).subscribe({
      next: d => { this.occupancyData = d; this.loading = false; },
      error: () => { this.loading = false; }
    });

    this.http.get<any[]>(`${base}/fraud-suspects`).subscribe({
      next: d => this.fraudSuspects = d,
      error: () => {}
    });
  }

  revenueBarWidth(revenue: number): number {
    return Math.round((Number(revenue) / this.maxRevenue) * 100);
  }

  exportCsv(): void {
    const token = localStorage.getItem('token');
    const url = `${environment.apiUrl}/api/v1/analytics/export/csv`;
    // Trigger download via anchor tag with auth header workaround
    fetch(url, { headers: { Authorization: `Bearer ${token}` } })
      .then(res => res.blob())
      .then(blob => {
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = 'bookings-export.csv';
        a.click();
        URL.revokeObjectURL(a.href);
      });
  }

  fraudRiskClass(rate: number): string {
    if (rate >= 80) return 'badge-danger';
    if (rate >= 50) return 'badge-warning';
    return 'badge-info';
  }
}
