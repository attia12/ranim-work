import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { Chart, registerables } from 'chart.js';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models/user.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private authService: AuthService,
    private router: Router
  ) {
    if (isPlatformBrowser(this.platformId)) {
      Chart.register(...registerables);
    }
  }

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user && user.role !== UserRole.ADMIN) {
      this.router.navigate([this.getRedirectLink(user.role)]);
      return;
    }

    // Vérifier qu'on est dans le navigateur avant de créer les graphiques
    if (isPlatformBrowser(this.platformId)) {
      // Attendre que le DOM soit prêt
      setTimeout(() => {
        this.createRevenueChart();
        this.createWeeklyChart();
      }, 0);
    }
  }

  // Revenue Overview Chart (Line Chart)
  createRevenueChart() {
    const ctx = document.getElementById('revenueChart') as HTMLCanvasElement;
    if (!ctx) return;

    new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
        datasets: [{
          label: 'Revenue',
          data: [15000, 18000, 20000, 22000, 25000, 28000],
          borderColor: '#28a745',
          backgroundColor: 'rgba(40, 167, 69, 0.1)',
          borderWidth: 3,
          tension: 0.4,
          fill: true,
          pointRadius: 5,
          pointBackgroundColor: '#28a745',
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointHoverRadius: 7
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            backgroundColor: '#1a1a1a',
            padding: 12,
            titleColor: '#fff',
            bodyColor: '#fff',
            displayColors: false,
            callbacks: {
              label: function (context) {
                return 'Revenue: $' + (context.parsed.y ?? 0).toLocaleString();
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: function (value) {
                return '$' + (value as number / 1000) + 'k';
              }
            },
            grid: {
              color: '#f0f0f0'
            }
          },
          x: {
            grid: {
              display: false
            }
          }
        }
      }
    });
  }

  // Weekly Bookings Chart (Bar Chart)
  createWeeklyChart() {
    const ctx = document.getElementById('weeklyChart') as HTMLCanvasElement;
    if (!ctx) return;

    new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
        datasets: [{
          label: 'Bookings',
          data: [45, 52, 48, 62, 75, 85, 68],
          backgroundColor: '#8B7355',
          borderRadius: 8,
          barThickness: 30
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            backgroundColor: '#1a1a1a',
            padding: 12,
            titleColor: '#fff',
            bodyColor: '#fff',
            displayColors: false
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              stepSize: 25
            },
            grid: {
              color: '#f0f0f0'
            }
          },
          x: {
            grid: {
              display: false
            }
          }
        }
      }
    });
  }

  private getRedirectLink(role: UserRole): string {
    switch (role) {
      case UserRole.GEAR_PROVIDER: return '/admin/gear-provider';
      case UserRole.CAMPSITE_OWNER: return '/admin/campsites';
      case UserRole.CAMPSITE_MANAGER: return '/admin/campsite-manager';
      case UserRole.SPONSOR: return '/admin/sponsor';
      case UserRole.DELIVERY_PERSONNEL: return '/admin/deliveries';
      case UserRole.FORUM_MODERATOR: return '/admin/forum-mod';
      case UserRole.GUIDE: return '/admin/guide';
      case UserRole.EVENT_ORGANIZER: return '/admin/events';
      default: return '/admin/dashboard';
    }
  }

}