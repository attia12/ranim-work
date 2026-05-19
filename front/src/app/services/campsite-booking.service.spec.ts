import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CampsiteBookingService } from './campsite-booking.service';
import { environment } from '../../environments/environment';

describe('CampsiteBookingService', () => {
  let service: CampsiteBookingService;
  let http: HttpTestingController;
  const bookingsUrl = `${environment.apiUrl}/api/v1/campsite-bookings`;
  const paymentsUrl = `${environment.apiUrl}/api/v1/campsite-payments`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CampsiteBookingService]
    });
    service = TestBed.inject(CampsiteBookingService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('create() calls POST /api/v1/campsite-bookings', () => {
    const req = { campsiteId: 1, checkInDate: '2026-06-01', checkOutDate: '2026-06-05', numberOfGuests: 2 };
    service.create(req).subscribe();
    const r = http.expectOne(bookingsUrl);
    expect(r.request.method).toBe('POST');
    expect(r.request.body).toEqual(req);
    r.flush({ id: 10, ...req, status: 'PENDING' });
  });

  it('getById() calls GET /api/v1/campsite-bookings/:id', () => {
    service.getById(10).subscribe();
    const r = http.expectOne(`${bookingsUrl}/10`);
    expect(r.request.method).toBe('GET');
    r.flush({ id: 10 });
  });

  it('getMyBookings() calls GET /api/v1/campsite-bookings/my', () => {
    service.getMyBookings(0, 10).subscribe();
    const r = http.expectOne(req => req.url === `${bookingsUrl}/my`);
    expect(r.request.method).toBe('GET');
    r.flush({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  });

  it('getByCampsite() calls GET /api/v1/campsite-bookings/campsite/:id', () => {
    service.getByCampsite(1, 0, 10).subscribe();
    const r = http.expectOne(req => req.url === `${bookingsUrl}/campsite/1`);
    expect(r.request.method).toBe('GET');
    r.flush({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  });

  it('getAll() calls GET /api/v1/campsite-bookings', () => {
    service.getAll(0, 10).subscribe();
    const r = http.expectOne(req => req.url === bookingsUrl);
    expect(r.request.method).toBe('GET');
    r.flush({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  });

  it('cancel() with reason sends body with reason', () => {
    service.cancel(5, 'changed plans').subscribe();
    const r = http.expectOne(`${bookingsUrl}/5/cancel`);
    expect(r.request.method).toBe('PATCH');
    expect(r.request.body).toEqual({ reason: 'changed plans' });
    r.flush({ id: 5, status: 'CANCELLED' });
  });

  it('cancel() without reason sends empty body', () => {
    service.cancel(5).subscribe();
    const r = http.expectOne(`${bookingsUrl}/5/cancel`);
    expect(r.request.body).toEqual({});
    r.flush({ id: 5, status: 'CANCELLED' });
  });

  it('confirm() calls PATCH /api/v1/campsite-bookings/:id/confirm', () => {
    service.confirm(5).subscribe();
    const r = http.expectOne(`${bookingsUrl}/5/confirm`);
    expect(r.request.method).toBe('PATCH');
    r.flush({ id: 5, status: 'CONFIRMED' });
  });

  it('pay() calls POST /api/v1/campsite-payments', () => {
    const req = { bookingId: 5, amount: 150, method: 'CARD' as any };
    service.pay(req).subscribe();
    const r = http.expectOne(paymentsUrl);
    expect(r.request.method).toBe('POST');
    expect(r.request.body).toEqual(req);
    r.flush({ id: 1, status: 'PAID' });
  });

  it('getPaymentByBooking() calls GET /api/v1/campsite-payments/booking/:id', () => {
    service.getPaymentByBooking(5).subscribe();
    const r = http.expectOne(`${paymentsUrl}/booking/5`);
    expect(r.request.method).toBe('GET');
    r.flush({ id: 1, bookingId: 5 });
  });
});
