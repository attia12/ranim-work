import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { CampsitePaymentComponent } from './campsite-payment.component';
import { CampsiteBookingService } from '../../../services/campsite-booking.service';

// Mock Stripe and canvas-confetti at module level
jest: {};
(window as any).confetti = () => {};

describe('CampsitePaymentComponent', () => {
  let component: CampsitePaymentComponent;
  let fixture: ComponentFixture<CampsitePaymentComponent>;
  let bookingService: jasmine.SpyObj<CampsiteBookingService>;

  const queryParams = { bookingId: '5', amount: '150' };

  beforeEach(async () => {
    bookingService = jasmine.createSpyObj('CampsiteBookingService', [
      'createPaymentIntent', 'pay'
    ]);
    bookingService.createPaymentIntent.and.returnValue(of({
      clientSecret: 'secret_123', publishableKey: 'pk_test_abc', paymentIntentId: 'pi_123'
    }));

    await TestBed.configureTestingModule({
      declarations: [CampsitePaymentComponent],
      imports: [RouterTestingModule, FormsModule],
      providers: [
        { provide: CampsiteBookingService, useValue: bookingService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: { get: (k: string) => queryParams[k as keyof typeof queryParams] } } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CampsitePaymentComponent);
    component = fixture.componentInstance;
    // Skip ngAfterViewInit Stripe mounting in tests
    spyOn<any>(component, 'mountCardElement').and.stub();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('reads bookingId and amount from query params on init', () => {
    expect(component.bookingId).toBe(5);
    expect(component.amount).toBe(150);
  });

  it('selectedMethod defaults to CARD', () => {
    expect(component.selectedMethod).toBe('CARD');
  });

  it('selectMethod() updates selectedMethod', () => {
    component.selectMethod('PAYPAL');
    expect(component.selectedMethod).toBe('PAYPAL');
  });

  it('selectMethod() clears error', () => {
    component.error = 'some error';
    component.selectMethod('BANK_TRANSFER');
    expect(component.error).toBe('');
  });

  it('pay() with PAYPAL calls payDirect via bookingService.pay()', () => {
    bookingService.pay.and.returnValue(of({ id: 1, status: 'PAID' } as any));
    component.selectedMethod = 'PAYPAL';
    component.pay();
    expect(bookingService.pay).toHaveBeenCalledWith(jasmine.objectContaining({
      bookingId: 5, amount: 150, method: 'PAYPAL'
    }));
    expect(component.paid).toBeTrue();
  });

  it('pay() with BANK_TRANSFER calls bookingService.pay() with BANK_TRANSFER', () => {
    bookingService.pay.and.returnValue(of({ id: 1, status: 'PAID' } as any));
    component.selectedMethod = 'BANK_TRANSFER';
    component.pay();
    expect(bookingService.pay).toHaveBeenCalledWith(jasmine.objectContaining({
      method: 'BANK_TRANSFER'
    }));
    expect(component.paid).toBeTrue();
  });

  it('pay() sets error on service failure (PAYPAL)', () => {
    bookingService.pay.and.returnValue(throwError(() => ({ error: { message: 'Payment failed' } })));
    component.selectedMethod = 'PAYPAL';
    component.pay();
    expect(component.error).toBe('Payment failed');
    expect(component.paid).toBeFalse();
  });

  it('goToBookings() navigates to /my-bookings', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    component.goToBookings();
    expect(router.navigate).toHaveBeenCalledWith(['/my-bookings']);
  });
});
