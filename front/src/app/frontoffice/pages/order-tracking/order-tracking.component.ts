import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { DeliveryService } from '../../../services/delivery.service';

@Component({
    selector: 'app-order-tracking',
    templateUrl: './order-tracking.component.html',
    styleUrl: './order-tracking.component.css'
})
export class OrderTrackingComponent implements OnInit, OnDestroy {
    orderId: number = 0;
    order: any = null;
    delivery: any = null;
    isLoading = true;
    error = '';
    lastRefreshed: Date = new Date();
    private refreshInterval: any;

    // Review state
    existingRating: any = null;   // null = not yet rated
    showReviewForm = false;
    isEditingRating = false;
    reviewRating = 0;             // 0 = no star selected
    reviewHover = 0;              // hover state for star animation
    reviewComment = '';
    reviewError = '';
    reviewSaving = false;
    reviewSuccess = false;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private http: HttpClient,
        private deliveryService: DeliveryService,
        @Inject(PLATFORM_ID) private platformId: Object
    ) {}

    ngOnInit(): void {
        this.orderId = parseInt(this.route.snapshot.paramMap.get('orderId') || '0', 10);
        this.loadOrderAndDelivery();

        if (isPlatformBrowser(this.platformId)) {
            this.refreshInterval = setInterval(() => {
                this.loadOrderAndDelivery();
            }, 30000);
        }
    }

    ngOnDestroy(): void {
        if (this.refreshInterval) clearInterval(this.refreshInterval);
    }

    loadOrderAndDelivery(): void {
        if (!this.order) this.isLoading = true;

        this.http.get<any>(`${environment.apiUrl}/api/marketplace/orders/${this.orderId}`)
            .subscribe({
                next: (order) => {
                    this.order = order;
                    this.lastRefreshed = new Date();
                    this.http.get<any>(`${environment.apiUrl}/api/delivery/deliveries/by-order/${this.orderId}`)
                        .subscribe({
                            next: (delivery) => {
                                this.delivery = delivery || null;
                                this.isLoading = false;
                                // Load existing rating when delivery is done
                                if (this.delivery?.status === 'DELIVERED' && !this.existingRating) {
                                    this.loadExistingRating();
                                }
                            },
                            error: () => { this.isLoading = false; }
                        });
                },
                error: () => { this.error = 'Could not load order.'; this.isLoading = false; }
            });
    }

    refresh(): void {
        this.loadOrderAndDelivery();
    }

    // ── Delivery Review ────────────────────────────────────────────────────

    loadExistingRating(): void {
        if (!this.delivery?.id) return;
        this.deliveryService.getRatingByDelivery(this.delivery.id).subscribe({
            next: (r) => { this.existingRating = r; },
            error: () => { this.existingRating = null; } // 404 = not rated yet
        });
    }

    openReviewForm(): void {
        this.showReviewForm = true;
        this.isEditingRating = false;
        this.reviewRating = 0;
        this.reviewComment = '';
        this.reviewError = '';
        this.reviewSuccess = false;
    }

    openEditForm(): void {
        this.showReviewForm = true;
        this.isEditingRating = true;
        this.reviewRating = this.existingRating.rating;
        this.reviewComment = this.existingRating.comment;
        this.reviewError = '';
        this.reviewSuccess = false;
    }

    cancelReview(): void {
        this.showReviewForm = false;
        this.reviewRating = 0;
        this.reviewComment = '';
        this.reviewError = '';
    }

    setRating(star: number): void {
        this.reviewRating = star;
    }

    submitReview(): void {
        this.reviewError = '';
        if (this.reviewRating === 0) {
            this.reviewError = 'Please select a star rating.';
            return;
        }
        if (!this.reviewComment.trim() || this.reviewComment.trim().length < 5) {
            this.reviewError = 'Comment must be at least 5 characters.';
            return;
        }

        this.reviewSaving = true;
        const obs = this.isEditingRating
            ? this.deliveryService.updateRating(this.existingRating.id, this.reviewRating, this.reviewComment.trim())
            : this.deliveryService.createRating(this.delivery.id, this.reviewRating, this.reviewComment.trim());

        obs.subscribe({
            next: (saved) => {
                this.reviewSaving = false;
                this.existingRating = saved;
                this.showReviewForm = false;
                this.reviewSuccess = true;
                setTimeout(() => this.reviewSuccess = false, 4000);
            },
            error: (err) => {
                this.reviewSaving = false;
                this.reviewError = err.error?.message || `Server error (${err.status}).`;
            }
        });
    }

    deleteReview(): void {
        if (!confirm('Delete your review?')) return;
        this.deliveryService.deleteRating(this.existingRating.id).subscribe({
            next: () => {
                this.existingRating = null;
                this.showReviewForm = false;
            },
            error: (err) => alert(err.error?.message || 'Could not delete review.')
        });
    }

    getRatingLabel(r: number): string {
        const labels = ['', 'Poor', 'Fair', 'Good', 'Very Good', 'Excellent'];
        return labels[r] || '';
    }

    // ── Status helpers ─────────────────────────────────────────────────────

    getOrderStatusStep(): number {
        if (!this.order) return 0;
        const steps: Record<string, number> = {
            DRAFT: 1, ACTIVE: 1, PENDING_PAYMENT: 1, PAID: 2,
            PROCESSING: 3, COMPLETED: 4, CANCELLED: 0
        };
        return steps[this.order.status] || 1;
    }

    getDeliveryStatusStep(): number {
        if (!this.delivery) return 0;
        const steps: Record<string, number> = {
            PENDING_ASSIGNMENT: 1, ASSIGNED: 2, AT_PICKUP: 3,
            PICKED_UP: 3, IN_TRANSIT: 4, DELIVERED: 5
        };
        return steps[this.delivery.status] || 1;
    }
}
