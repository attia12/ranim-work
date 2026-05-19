import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CampsiteService } from './campsite.service';
import { environment } from '../../environments/environment';

describe('CampsiteService', () => {
  let service: CampsiteService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/api/v1/campsites`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CampsiteService]
    });
    service = TestBed.inject(CampsiteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('search() calls GET /api/v1/campsites with params', () => {
    service.search({ country: 'Tunisia', city: 'Tunis', page: 0, size: 10 }).subscribe();
    const req = http.expectOne(r => r.url === base);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('country')).toBe('Tunisia');
    expect(req.request.params.get('city')).toBe('Tunis');
    req.flush({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  });

  it('search() omits undefined filters', () => {
    service.search({ page: 0, size: 5 }).subscribe();
    const req = http.expectOne(r => r.url === base);
    expect(req.request.params.has('country')).toBeFalse();
    expect(req.request.params.has('city')).toBeFalse();
    req.flush({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  });

  it('getCampsiteById() calls GET /api/v1/campsites/:id', () => {
    service.getCampsiteById(42).subscribe();
    const req = http.expectOne(`${base}/42`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 42, name: 'Test' });
  });

  it('getMyCampsites() calls GET /api/v1/campsites/my', () => {
    service.getMyCampsites(0, 10).subscribe();
    const req = http.expectOne(r => r.url === `${base}/my`);
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  });

  it('getAllAdmin() calls GET /api/v1/campsites/all', () => {
    service.getAllAdmin(0, 10).subscribe();
    const req = http.expectOne(r => r.url === `${base}/all`);
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  });

  it('create() calls POST /api/v1/campsites', () => {
    const payload = { name: 'New Camp', country: 'Tunisia', city: 'Tunis' } as any;
    service.create(payload).subscribe();
    const req = http.expectOne(base);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({ id: 1, ...payload });
  });

  it('update() calls PUT /api/v1/campsites/:id', () => {
    const payload = { name: 'Updated' } as any;
    service.update(7, payload).subscribe();
    const req = http.expectOne(`${base}/7`);
    expect(req.request.method).toBe('PUT');
    req.flush({ id: 7, ...payload });
  });

  it('delete() calls DELETE /api/v1/campsites/:id', () => {
    service.delete(3).subscribe();
    const req = http.expectOne(`${base}/3`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('suspend() calls PATCH /api/v1/campsites/:id/suspend', () => {
    service.suspend(5).subscribe();
    const req = http.expectOne(`${base}/5/suspend`);
    expect(req.request.method).toBe('PATCH');
    req.flush({ id: 5, status: 'SUSPENDED' });
  });

  it('activate() calls PATCH /api/v1/campsites/:id/activate', () => {
    service.activate(5).subscribe();
    const req = http.expectOne(`${base}/5/activate`);
    expect(req.request.method).toBe('PATCH');
    req.flush({ id: 5, status: 'ACTIVE' });
  });
});
