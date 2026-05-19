import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { OutdoorCampsiteService } from './outdoor-campsite.service';
import { environment } from '../../environments/environment';

describe('OutdoorCampsiteService', () => {
  let service: OutdoorCampsiteService;
  let http: HttpTestingController;
  const sitesUrl    = `${environment.apiUrl}/api/v1/outdoor-campsites`;
  const bookingsUrl = `${environment.apiUrl}/api/v1/outdoor-bookings`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [OutdoorCampsiteService]
    });
    service = TestBed.inject(OutdoorCampsiteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getApproved() calls GET /api/v1/outdoor-campsites', () => {
    service.getApproved(0, 10).subscribe();
    const r = http.expectOne(req => req.url === sitesUrl);
    expect(r.request.method).toBe('GET');
    r.flush({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  });

  it('getById() calls GET /api/v1/outdoor-campsites/:id', () => {
    service.getById(3).subscribe();
    const r = http.expectOne(`${sitesUrl}/3`);
    expect(r.request.method).toBe('GET');
    r.flush({ id: 3, name: 'Hidden Valley' });
  });

  it('propose() calls POST /api/v1/outdoor-campsites', () => {
    const req = { name: 'Wild Camp', country: 'Tunisia', city: 'Beja' } as any;
    service.propose(req).subscribe();
    const r = http.expectOne(sitesUrl);
    expect(r.request.method).toBe('POST');
    expect(r.request.body).toEqual(req);
    r.flush({ id: 1, ...req, status: 'PENDING' });
  });

  it('update() calls PUT /api/v1/outdoor-campsites/:id', () => {
    const req = { name: 'Updated Camp' } as any;
    service.update(3, req).subscribe();
    const r = http.expectOne(`${sitesUrl}/3`);
    expect(r.request.method).toBe('PUT');
    r.flush({ id: 3, ...req });
  });

  it('getPending() calls GET /api/v1/outdoor-campsites/pending', () => {
    service.getPending(0, 10).subscribe();
    const r = http.expectOne(req => req.url === `${sitesUrl}/pending`);
    expect(r.request.method).toBe('GET');
    r.flush({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  });

  it('moderate() calls PATCH /api/v1/outdoor-campsites/:id/moderate', () => {
    service.moderate(3, { action: 'APPROVE' }).subscribe();
    const r = http.expectOne(`${sitesUrl}/3/moderate`);
    expect(r.request.method).toBe('PATCH');
    expect(r.request.body).toEqual({ action: 'APPROVE' });
    r.flush({ id: 3, status: 'APPROVED' });
  });

  it('moderate() sends adminNote on reject', () => {
    service.moderate(3, { action: 'REJECT', adminNote: 'Too remote' }).subscribe();
    const r = http.expectOne(`${sitesUrl}/3/moderate`);
    expect(r.request.body.adminNote).toBe('Too remote');
    r.flush({ id: 3, status: 'REJECTED' });
  });

  it('getMyProposals() calls GET /api/v1/outdoor-campsites/my', () => {
    service.getMyProposals(0, 10).subscribe();
    const r = http.expectOne(req => req.url === `${sitesUrl}/my`);
    expect(r.request.method).toBe('GET');
    r.flush({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  });

  it('book() calls POST /api/v1/outdoor-bookings', () => {
    const req = { outdoorCampsiteId: 3, checkInDate: '2026-07-01', checkOutDate: '2026-07-05', numberOfGuests: 2 };
    service.book(req).subscribe();
    const r = http.expectOne(bookingsUrl);
    expect(r.request.method).toBe('POST');
    expect(r.request.body).toEqual(req);
    r.flush({ id: 1, status: 'CONFIRMED' });
  });

  it('getBookingById() calls GET /api/v1/outdoor-bookings/:id', () => {
    service.getBookingById(7).subscribe();
    const r = http.expectOne(`${bookingsUrl}/7`);
    expect(r.request.method).toBe('GET');
    r.flush({ id: 7 });
  });

  it('getMyOutdoorBookings() calls GET /api/v1/outdoor-bookings/my', () => {
    service.getMyOutdoorBookings(0, 10).subscribe();
    const r = http.expectOne(req => req.url === `${bookingsUrl}/my`);
    expect(r.request.method).toBe('GET');
    r.flush({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  });

  it('cancelBooking() calls PATCH /api/v1/outdoor-bookings/:id/cancel', () => {
    service.cancelBooking(7).subscribe();
    const r = http.expectOne(`${bookingsUrl}/7/cancel`);
    expect(r.request.method).toBe('PATCH');
    r.flush({ id: 7, status: 'CANCELLED' });
  });
});
