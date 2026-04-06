// Module: Official Campsite & Booking | Layer: Frontend Component (Smart)
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CampsiteBookingService } from '../../../services/campsite-booking.service';
import confetti from 'canvas-confetti';

@Component({
  selector: 'app-campsite-payment',
  templateUrl: './campsite-payment.component.html',
  styleUrl: './campsite-payment.component.css'
})
export class CampsitePaymentComponent implements OnInit, OnDestroy {

  bookingId: number = 0;
  amount: number = 0;

  selectedMethod: 'CARD' | 'PAYPAL' | 'BANK_TRANSFER' = 'CARD';

  cardNumber = '';
  cardName = '';
  cardExpiry = '';
  cardCVV = '';
  cardBrandIcon = 'fa-credit-card';
  expiryExpired = false;

  processing = false;
  paid = false;
  error = '';

  private confettiInterval: any;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: CampsiteBookingService
  ) {}

  ngOnInit(): void {
    this.bookingId = Number(this.route.snapshot.queryParamMap.get('bookingId'));
    this.amount    = Number(this.route.snapshot.queryParamMap.get('amount'));
  }

  ngOnDestroy(): void {
    clearInterval(this.confettiInterval);
  }

  selectMethod(method: 'CARD' | 'PAYPAL' | 'BANK_TRANSFER'): void {
    this.selectedMethod = method;
    this.error = '';
  }

  formatCardNumber(event: Event): void {
    const input = event.target as HTMLInputElement;
    const raw = input.value.replace(/\D/g, '').slice(0, 16);
    this.cardNumber = raw.replace(/(.{4})/g, '$1 ').trim();
    input.value = this.cardNumber;
    if (/^4/.test(raw))               this.cardBrandIcon = 'fa-cc-visa';
    else if (/^5[1-5]/.test(raw))     this.cardBrandIcon = 'fa-cc-mastercard';
    else                              this.cardBrandIcon = 'fa-credit-card';
  }

  formatExpiry(event: Event): void {
    const input = event.target as HTMLInputElement;
    const raw = input.value.replace(/\D/g, '').slice(0, 4);
    this.cardExpiry = raw.length >= 3 ? raw.slice(0, 2) + '/' + raw.slice(2) : raw;
    input.value = this.cardExpiry;
    const match = this.cardExpiry.match(/^(\d{2})\/(\d{2})$/);
    if (match) {
      const month = parseInt(match[1], 10);
      const year  = 2000 + parseInt(match[2], 10);
      this.expiryExpired = new Date(year, month, 1) <= new Date();
    } else {
      this.expiryExpired = false;
    }
  }

  sanitizeCVV(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.replace(/\D/g, '');
    this.cardCVV = input.value;
  }

  get isCardValid(): boolean {
    if (this.selectedMethod !== 'CARD') return true;
    return this.cardNumber.replace(/\s/g, '').length === 16
      && this.cardName.trim().length >= 3
      && /^\d{2}\/\d{2}$/.test(this.cardExpiry)
      && !this.expiryExpired
      && this.cardCVV.length >= 3;
  }

  pay(): void {
    if (!this.isCardValid) {
      this.error = 'Please fill in all card details correctly.';
      return;
    }
    this.processing = true;
    this.error = '';

    this.bookingService.pay({
      bookingId:     this.bookingId,
      amount:        this.amount,
      method:        this.selectedMethod,
      transactionId: 'TXN-' + Date.now()
    }).subscribe({
      next: () => {
        this.processing = false;
        this.paid = true;
        this.launchConfetti();
      },
      error: (err) => {
        this.processing = false;
        this.error = err.error?.message || err.error?.error || 'Payment failed. Please try again.';
      }
    });
  }

  goToBookings(): void {
    clearInterval(this.confettiInterval);
    this.router.navigate(['/my-bookings']);
  }

  private launchConfetti(): void {
    const duration = 4000;
    const end = Date.now() + duration;

    const frame = () => {
      confetti({
        particleCount: 6,
        angle: 60,
        spread: 55,
        origin: { x: 0 },
        colors: ['#28a745', '#007bff', '#ffc107', '#dc3545', '#6f42c1']
      });
      confetti({
        particleCount: 6,
        angle: 120,
        spread: 55,
        origin: { x: 1 },
        colors: ['#28a745', '#007bff', '#ffc107', '#dc3545', '#6f42c1']
      });
      if (Date.now() < end) {
        requestAnimationFrame(frame);
      }
    };
    frame();
  }
}
