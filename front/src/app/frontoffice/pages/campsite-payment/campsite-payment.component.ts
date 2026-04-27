// Module: Official Campsite & Booking | Layer: Frontend Component (Smart)
import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CampsiteBookingService } from '../../../services/campsite-booking.service';
import { loadStripe, Stripe, StripeCardElement } from '@stripe/stripe-js';
import confetti from 'canvas-confetti';

@Component({
  selector: 'app-campsite-payment',
  templateUrl: './campsite-payment.component.html',
  styleUrl: './campsite-payment.component.css'
})
export class CampsitePaymentComponent implements OnInit, AfterViewInit, OnDestroy {

  bookingId: number = 0;
  amount: number = 0;

  selectedMethod: 'CARD' | 'PAYPAL' | 'BANK_TRANSFER' = 'CARD';

  cardName = '';

  processing = false;
  paid = false;
  error = '';

  private stripe: Stripe | null = null;
  private cardElement: StripeCardElement | null = null;
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

  ngAfterViewInit(): void {
    if (this.selectedMethod === 'CARD') {
      this.mountCardElement();
    }
  }

  ngOnDestroy(): void {
    clearInterval(this.confettiInterval);
    if (this.cardElement) {
      this.cardElement.destroy();
      this.cardElement = null;
    }
  }

  selectMethod(method: 'CARD' | 'PAYPAL' | 'BANK_TRANSFER'): void {
    this.selectedMethod = method;
    this.error = '';
    if (method === 'CARD') {
      setTimeout(() => this.mountCardElement(), 0);
    } else {
      if (this.cardElement) {
        this.cardElement.destroy();
        this.cardElement = null;
      }
    }
  }

  private mountCardElement(): void {
    if (this.cardElement) return; // already mounted

    // Get publishable key from backend, then mount
    this.bookingService.createPaymentIntent(this.bookingId, this.amount).subscribe({
      next: async (data) => {
        this.stripe = await loadStripe(data.publishableKey);
        if (!this.stripe) { this.error = 'Stripe failed to load.'; return; }

        const elements = this.stripe.elements();
        this.cardElement = elements.create('card', {
          style: {
            base: {
              fontSize: '16px',
              color: '#32325d',
              fontFamily: '"Helvetica Neue", Helvetica, sans-serif',
              '::placeholder': { color: '#aab7c4' }
            },
            invalid: { color: '#dc3545' }
          }
        });
        const container = document.getElementById('stripe-card-element');
        if (container) {
          this.cardElement.mount(container);
        }

        // Store clientSecret for later use during pay()
        (this as any)._clientSecret = data.clientSecret;
        (this as any)._paymentIntentId = data.paymentIntentId;
      },
      error: () => {
        this.error = 'Could not initialize payment. Please try again.';
      }
    });
  }

  pay(): void {
    if (this.selectedMethod === 'CARD') {
      this.payWithStripe();
    } else {
      this.payDirect();
    }
  }

  private payWithStripe(): void {
    if (!this.stripe || !this.cardElement) {
      this.error = 'Card not ready yet. Please wait a moment.';
      return;
    }
    const clientSecret = (this as any)._clientSecret;
    if (!clientSecret) {
      this.error = 'Payment session expired. Please refresh.';
      return;
    }

    this.processing = true;
    this.error = '';

    this.stripe.confirmCardPayment(clientSecret, {
      payment_method: {
        card: this.cardElement,
        billing_details: { name: this.cardName || 'Cardholder' }
      }
    }).then(result => {
      if (result.error) {
        this.processing = false;
        this.error = result.error.message || 'Card payment failed.';
      } else if (result.paymentIntent?.status === 'succeeded') {
        // Record the payment in our backend
        this.bookingService.pay({
          bookingId:     this.bookingId,
          amount:        this.amount,
          method:        'CARD',
          transactionId: result.paymentIntent.id
        }).subscribe({
          next: () => {
            this.processing = false;
            this.paid = true;
            this.launchConfetti();
          },
          error: (err) => {
            this.processing = false;
            this.error = err.error?.message || 'Payment succeeded but booking confirmation failed. Contact support.';
          }
        });
      }
    });
  }

  private payDirect(): void {
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
