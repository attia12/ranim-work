import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CartService, CartItem } from '../../../services/cart.service';
import { AuthService } from '../../../services/auth.service';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css'
})
export class CartComponent implements OnInit {
  items: CartItem[] = [];
  total: number = 0;

  // Checkout modal state
  showConfirmModal = false;
  isCheckingOut = false;
  checkoutError = '';

  couponCode = '';
  couponDiscount = 0;
  couponError = '';
  couponApplied = false;
  isApplyingCoupon = false;

  applyCoupon() {
    if (!this.couponCode.trim()) return;
    this.isApplyingCoupon = true;
    this.couponError = '';
    this.http.post<any>(`${environment.apiUrl}/api/marketplace/coupons/validate`, { code: this.couponCode })
      .subscribe({
        next: (res) => {
          this.couponDiscount = res.discountAmount || 0;
          this.couponApplied = true;
          this.isApplyingCoupon = false;
          this.calculateTotal();
        },
        error: (err) => {
          this.couponError = err.error?.message || 'Invalid or expired coupon.';
          this.couponApplied = false;
          this.couponDiscount = 0;
          this.isApplyingCoupon = false;
        }
      });
  }

  constructor(
    private cartService: CartService,
    private authService: AuthService,
    private http: HttpClient,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit() {
    this.cartService.items$.subscribe(items => {
      this.items = items;
      this.calculateTotal();
    });
  }

  calculateTotal() {
    this.total = this.items.reduce((acc, item) => acc + this.getItemPrice(item) * item.quantity, 0);
    this.total = this.total - this.couponDiscount;
  }

  getItemPrice(item: CartItem): number {
    return item.transactionType === 'RENT'
      ? (item.pricePerDay || 0) * (item.rentalDays || 1)
      : (item.purchasePrice || item.price || 0);
  }

  updateQuantity(item: CartItem, quantity: number) {
    if (quantity < 1) return;
    this.cartService.updateQuantity(item.id, quantity);
  }

  removeItem(item: CartItem) {
    this.cartService.removeFromCart(item.id, item.transactionType, item.rentalDays);
  }

  clearCart() {
    this.cartService.clearCart();
    this.showConfirmModal = false;
  }

  // Step 1: open the modal — does NOT call the API yet
  openCheckoutModal() {
    const user = this.authService.getCurrentUser();
    if (!user) { this.router.navigate(['/login']); return; }
    this.checkoutError = '';
    this.showConfirmModal = true;
  }

  closeModal() {
    if (!this.isCheckingOut) {
      this.showConfirmModal = false;
      this.checkoutError = '';
    }
  }

  // Step 2: user confirmed — now call the API
  confirmOrder() {
    this.isCheckingOut = true;
    this.checkoutError = '';

    const orderRequest = {
      type: 'CART',
      notes: '',
      items: this.items.map(item => ({
        equipmentId: parseInt(item.id, 10),
        quantity: item.quantity,
        transactionType: item.transactionType,
        rentalDays: item.transactionType === 'RENT' ? (item.rentalDays || 1) : null,
        rentalStartDate: item.transactionType === 'RENT' ? (item.rentalStartDate || null) : null,
        rentalEndDate:   item.transactionType === 'RENT' ? (item.rentalEndDate  || null) : null,
      }))
    };

    this.http.post<any>(`${environment.apiUrl}/api/marketplace/orders`, orderRequest)
      .subscribe({
        next: (order) => {
          this.isCheckingOut = false;
          this.showConfirmModal = false;
          this.cartService.clearCart();
          this.router.navigate(['/payment', order.id]);
        },
        error: (err) => {
          this.isCheckingOut = false;
          this.checkoutError = err.error?.message || 'Order failed. Please try again.';
        }
      });
  }
}
