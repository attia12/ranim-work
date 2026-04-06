import { Component, OnInit, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NotificationService } from '../../../services/notification.service';

type PaymentMethodKey = 'BANK_CARD' | 'D17';

@Component({
  selector: 'app-payment',
  templateUrl: './payment.component.html',
  styleUrl: './payment.component.css'
})
export class PaymentComponent implements OnInit {
  @ViewChild('cardForm') cardForm?: NgForm;

  orderId: number = 0;
  order: any = null;
  isLoadingOrder = true;
  orderError = '';

  selectedMethod: PaymentMethodKey = 'BANK_CARD';
  isProcessing = false;
  paymentError = '';

  paymentMethods: { key: PaymentMethodKey; label: string; icon: string; description: string }[] = [
    {
      key: 'BANK_CARD',
      label: 'Carte Bancaire',
      icon: 'fas fa-credit-card',
      description: 'Pay with your Tunisian bank card (CIB / Visa / Mastercard)'
    },
    {
      key: 'D17',
      label: 'D17 — La Poste Tunisienne',
      icon: 'fas fa-university',
      description: 'Pay via D17 mobile wallet. You will be redirected to complete payment.'
    }
  ];

  cardNumber = '';
  cardName = '';
  cardExpiry = '';
  cardCVV = '';
  cardBrandIcon = 'fa-credit-card';
  expiryExpired = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private notifService: NotificationService
  ) { }

  ngOnInit() {
    this.orderId = parseInt(this.route.snapshot.paramMap.get('orderId') || '0', 10);
    this.loadOrder();
  }

  loadOrder() {
    this.http.get<any>(`${environment.apiUrl}/api/marketplace/orders/${this.orderId}`)
      .subscribe({
        next: (order) => { this.order = order; this.isLoadingOrder = false; },
        error: () => { this.orderError = 'Could not load order details.'; this.isLoadingOrder = false; }
      });
  }

  selectMethod(method: PaymentMethodKey) {
    this.selectedMethod = method;
    this.paymentError = '';
  }

  formatCardNumber(event: Event) {
    const input = event.target as HTMLInputElement;
    const raw = input.value.replace(/\D/g, '').slice(0, 16);
    this.cardNumber = raw.replace(/(.{4})/g, '$1 ').trim();
    input.value = this.cardNumber;

    if (/^4/.test(raw)) {
      this.cardBrandIcon = 'fa-cc-visa';
    } else if (/^5[1-5]/.test(raw) || /^2[2-7]/.test(raw)) {
      this.cardBrandIcon = 'fa-cc-mastercard';
    } else if (/^3[47]/.test(raw)) {
      this.cardBrandIcon = 'fa-cc-amex';
    } else {
      this.cardBrandIcon = 'fa-credit-card';
    }
  }

  formatExpiry(event: Event) {
    const input = event.target as HTMLInputElement;
    const raw = input.value.replace(/\D/g, '').slice(0, 4);
    this.cardExpiry = raw.length >= 3 ? raw.slice(0, 2) + '/' + raw.slice(2) : raw;
    input.value = this.cardExpiry;
    this.checkExpiryDate();
  }

  /** Strip non-digits from CVV — called from template via (input) */
  sanitizeCVV(event: Event) {
    const input = event.target as HTMLInputElement;
    input.value = input.value.replace(/[^0-9]/g, '');
    this.cardCVV = input.value;
  }

  private checkExpiryDate() {
    const match = this.cardExpiry.match(/^(\d{2})\/(\d{2})$/);
    if (!match) { this.expiryExpired = false; return; }
    const month = parseInt(match[1], 10);
    const year = 2000 + parseInt(match[2], 10);
    this.expiryExpired = new Date(year, month, 1) <= new Date();
  }

  pay() {
    this.paymentError = '';

    if (this.selectedMethod === 'BANK_CARD') {
      if (this.cardForm) {
        this.cardForm.control.markAllAsTouched();
        if (this.cardForm.invalid) {
          this.paymentError = 'Please fix the errors in the card form before proceeding.';
          return;
        }
      }
      if (this.expiryExpired) {
        this.paymentError = 'Your card has expired. Please use a valid card.';
        return;
      }
    }

    this.isProcessing = true;

    const paymentRequest = {
      orderId: this.orderId,
      paymentMethod: this.selectedMethod,
      transactionId: 'TXN-' + Date.now()
    };

    this.http.post<any>(`${environment.apiUrl}/api/marketplace/payments`, paymentRequest)
      .subscribe({
        next: () => {
          this.isProcessing = false;
          this.notifService.push({
            type: 'order',
            title: 'Order Confirmed! 🎉',
            message: 'Your payment was successful. We\'re preparing your order.',
            icon: 'fa-check-circle',
            iconColor: '#10b981',
            link: `/orders/${this.orderId}`
          });
          this.router.navigate(['/orders', this.orderId]);
        },
        error: (err) => {
          this.isProcessing = false;
          this.paymentError = err.error?.message || 'Payment failed. Please try again.';
        }
      });
  }

  formatAmount(amount: number): string {
    return 'TND ' + (amount || 0).toFixed(3);
  }
}
