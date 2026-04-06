import { Component, OnInit } from '@angular/core';
import { DeliveryService } from '../../../services/delivery.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-delivery',
  templateUrl: './delivery.component.html',
  styleUrl: './delivery.component.css'
})
export class DeliveryComponent implements OnInit {
  orderId: string = '';
  step: number = 1;
  status: string = 'Ordered';
  deliveryData: any = null;
  isLoading = true;
  error = '';

  constructor(
    private deliveryService: DeliveryService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user && user.id) {
      this.loadDeliveryData(user.id);
    } else {
      this.error = 'User not authenticated';
      this.isLoading = false;
    }
  }

  private loadDeliveryData(userId: string): void {
    this.deliveryService.getLatestDelivery(userId).subscribe({
      next: (delivery: any) => {
        if (delivery) {
          this.deliveryData = delivery;
          this.orderId = delivery.orderId || '';
          this.step = this.mapStatusToStep(delivery.status);
          this.status = this.formatStatus(delivery.status);
        }
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Failed to load delivery data:', err);
        this.error = 'Could not load delivery information. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  private mapStatusToStep(status: string): number {
    const statusMap: { [key: string]: number } = {
      'ORDERED': 1,
      'PROCESSING': 2,
      'IN_TRANSIT': 3,
      'DELIVERED': 4
    };
    return statusMap[status?.toUpperCase()] || 1;
  }

  private formatStatus(status: string): string {
    const statusMap: { [key: string]: string } = {
      'ORDERED': 'Ordered',
      'PROCESSING': 'Processing',
      'IN_TRANSIT': 'In Transit',
      'DELIVERED': 'Delivered'
    };
    return statusMap[status?.toUpperCase()] || status;
  }
}
