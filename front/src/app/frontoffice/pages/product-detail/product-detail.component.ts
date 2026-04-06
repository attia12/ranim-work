import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../../services/product.service';
import { CartService } from '../../../services/cart.service';
import { AuthService } from '../../../services/auth.service';
import { Product } from '../../../models/product.model';

@Component({
  selector: 'app-product-detail',
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.css'
})
export class ProductDetailComponent implements OnInit {
  product: Product | undefined;
  reviews: any[] = [];
  isLoadingReviews = true;
  currentUserId: string | null = null;

  // New review form
  newReview = { rating: 5, comment: '' };
  isSubmittingReview = false;
  reviewError = '';
  reviewSuccess = '';

  // Edit review state
  editingReviewId: number | null = null;
  editReview = { rating: 5, comment: '' };
  isUpdatingReview = false;
  editError = '';

  // Rent/Buy mode
  selectedMode: 'RENT' | 'BUY' = 'BUY';
  rentalStartDate: string = '';
  rentalEndDate: string = '';
  rentalDays: number = 0;
  unavailablePeriods: any[] = [];
  minDate: string = new Date().toISOString().split('T')[0]; // today
  cartSuccess = '';
  rentalConflict = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
    private authService: AuthService
  ) { }

  ngOnInit() {
    const user = this.authService.getCurrentUser();
    this.currentUserId = user?.id ?? null;

    this.route.params.subscribe(params => {
      const id = params['id'];
      if (id) {
        this.productService.getProductById(id).subscribe({
          next: (product) => {
            this.product = product;
            if (product.availableForRent && !product.availableForSale) {
              this.selectedMode = 'RENT';
            } else {
              this.selectedMode = 'BUY';
            }
            this.loadReviews(id);
            this.productService.getUnavailablePeriods(product.id)
              .subscribe({ next: p => this.unavailablePeriods = p, error: () => {} });
          },
          error: () => this.router.navigate(['/shop'])
        });
      }
    });
  }

  loadReviews(productId: string) {
    this.isLoadingReviews = true;
    this.productService.getReviewsByEquipment(productId).subscribe({
      next: (reviews) => { this.reviews = reviews; this.isLoadingReviews = false; },
      error: () => { this.isLoadingReviews = false; }
    });
  }

  isMyReview(review: any): boolean {
    return this.currentUserId !== null && review.authorId === this.currentUserId;
  }

  startEdit(review: any) {
    this.editingReviewId = review.id;
    this.editReview = { rating: review.rating, comment: review.comment };
    this.editError = '';
  }

  cancelEdit() {
    this.editingReviewId = null;
    this.editError = '';
  }

  setEditRating(rating: number) {
    this.editReview.rating = rating;
  }

  saveEdit() {
    if (!this.editReview.comment.trim()) {
      this.editError = 'Comment cannot be empty.';
      return;
    }
    this.isUpdatingReview = true;
    this.editError = '';

    this.productService.updateReview(this.editingReviewId!, {
      equipmentId: parseInt(this.product!.id, 10),
      rating: this.editReview.rating,
      comment: this.editReview.comment
    }).subscribe({
      next: () => {
        this.isUpdatingReview = false;
        this.editingReviewId = null;
        this.loadReviews(this.product!.id);
      },
      error: (err) => {
        this.editError = err.error?.message || 'Failed to update review.';
        this.isUpdatingReview = false;
      }
    });
  }

  get averageRating(): number {
    if (!this.reviews.length) return 0;
    return this.reviews.reduce((sum, r) => sum + r.rating, 0) / this.reviews.length;
  }

  get totalPrice(): number {
    if (!this.product) return 0;
    if (this.selectedMode === 'RENT') return (this.product.pricePerDay || 0) * this.rentalDays;
    return this.product.purchasePrice || this.product.price || 0;
  }

  setMode(mode: 'RENT' | 'BUY') { this.selectedMode = mode; }

  onDateChange() {
    if (this.rentalStartDate && this.rentalEndDate) {
      const start = new Date(this.rentalStartDate);
      const end   = new Date(this.rentalEndDate);
      if (end <= start) { this.rentalEndDate = ''; this.rentalDays = 0; return; }
      this.rentalDays = Math.ceil(
        (end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)
      );
      // Check if selected range overlaps any unavailable period
      this.rentalConflict = this.unavailablePeriods.some(p => {
        const pStart = new Date(p.startDate);
        const pEnd   = new Date(p.endDate);
        return start < pEnd && end > pStart;
      });
    }
  }

  addToCart() {
    if (!this.product) return;
    const user = this.authService.getCurrentUser();
    if (!user) { this.router.navigate(['/login']); return; }

    if (this.selectedMode === 'RENT' && (!this.rentalStartDate || !this.rentalEndDate)) {
      alert('Please select rental start and end dates.');
      return;
    }

    this.cartService.addToCart(
      this.product,
      this.selectedMode,
      this.selectedMode === 'RENT' ? this.rentalDays : undefined,
      this.selectedMode === 'RENT' ? this.rentalStartDate : undefined,
      this.selectedMode === 'RENT' ? this.rentalEndDate : undefined
    );
    this.cartSuccess = `${this.product.name} added to cart!`;
    setTimeout(() => this.cartSuccess = '', 3000);
  }

  submitReview() {
    const user = this.authService.getCurrentUser();
    if (!user) { this.router.navigate(['/login']); return; }
    if (!this.newReview.comment.trim()) { this.reviewError = 'Please enter a comment.'; return; }

    this.isSubmittingReview = true;
    this.reviewError = '';
    this.reviewSuccess = '';

    this.productService.addReview(this.product!.id, {
      rating: this.newReview.rating,
      comment: this.newReview.comment
    }).subscribe({
      next: () => {
        this.reviewSuccess = 'Review submitted!';
        this.newReview = { rating: 5, comment: '' };
        this.isSubmittingReview = false;
        this.loadReviews(this.product!.id);
      },
      error: (err) => {
        this.reviewError = err.error?.message || 'Failed to submit review.';
        this.isSubmittingReview = false;
      }
    });
  }

  setRating(rating: number) { this.newReview.rating = rating; }
}
