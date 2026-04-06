import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { CartService } from './cart.service';
import { NotificationService } from './notification.service';
import { PLATFORM_ID } from '@angular/core';
import { UserRole } from '../models/user.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let cartServiceSpy: jasmine.SpyObj<CartService>;
  let notifServiceSpy: jasmine.SpyObj<NotificationService>;

  beforeEach(() => {
    cartServiceSpy = jasmine.createSpyObj('CartService', ['clearCartOnLogout']);
    notifServiceSpy = jasmine.createSpyObj('NotificationService', ['initForUser', 'clearSession', 'push']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        { provide: CartService, useValue: cartServiceSpy },
        { provide: NotificationService, useValue: notifServiceSpy },
        { provide: PLATFORM_ID, useValue: 'browser' }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  // ── isLoggedIn ─────────────────────────────────────────────────────────────

  it('should return false when no user is logged in', () => {
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('should return true after login', () => {
    const mockResponse = {
      token: 'fake-token',
      userId: 1,
      fullname: 'John Doe',
      role: UserRole.CAMPER,
      phoneNumber: '123456789'
    };

    service.login('test@test.com', 'password').subscribe();
    const req = httpMock.expectOne('http://localhost:9099/auth/login');
    req.flush(mockResponse);
    const meReq = httpMock.match('http://localhost:9099/auth/me');
    meReq.forEach(r => r.flush(mockResponse));

    expect(service.isLoggedIn()).toBeTrue();
  });

  // ── login ──────────────────────────────────────────────────────────────────

  it('should store token in localStorage on login', () => {
    const mockResponse = {
      token: 'my-jwt-token',
      userId: 42,
      fullname: 'Jane',
      role: UserRole.CAMPER,
      phoneNumber: ''
    };

    service.login('jane@test.com', 'pass').subscribe();
    httpMock.expectOne('http://localhost:9099/auth/login').flush(mockResponse);
    httpMock.match('http://localhost:9099/auth/me').forEach(r => r.flush(mockResponse));

    expect(localStorage.getItem('token')).toBe('my-jwt-token');
  });

  it('should store userId in localStorage on login', () => {
    const mockResponse = {
      token: 'tok',
      userId: 99,
      fullname: 'Bob',
      role: UserRole.ADMIN,
      phoneNumber: ''
    };

    service.login('bob@test.com', 'pass').subscribe();
    httpMock.expectOne('http://localhost:9099/auth/login').flush(mockResponse);
    httpMock.match('http://localhost:9099/auth/me').forEach(r => r.flush(mockResponse));

    expect(localStorage.getItem('userId')).toBe('99');
  });

  it('should call notifService.initForUser on login', () => {
    const mockResponse = { token: 'tok', userId: 5, fullname: 'Ali', role: UserRole.CAMPER };
    service.login('ali@test.com', 'pass').subscribe();
    httpMock.expectOne('http://localhost:9099/auth/login').flush(mockResponse);
    httpMock.match('http://localhost:9099/auth/me').forEach(r => r.flush(mockResponse));
    expect(notifServiceSpy.initForUser).toHaveBeenCalledWith('5');
  });

  // ── logout ─────────────────────────────────────────────────────────────────

  it('should clear localStorage on logout', () => {
    localStorage.setItem('token', 'tok');
    localStorage.setItem('currentUser', '{}');
    service.logout();
    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('currentUser')).toBeNull();
  });

  it('should call cartService.clearCartOnLogout on logout', () => {
    service.logout();
    expect(cartServiceSpy.clearCartOnLogout).toHaveBeenCalled();
  });

  it('should call notifService.clearSession on logout', () => {
    service.logout();
    expect(notifServiceSpy.clearSession).toHaveBeenCalled();
  });

  it('should set currentUser to null on logout', () => {
    service.logout();
    expect(service.getCurrentUser()).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
  });

  // ── getCurrentUser ─────────────────────────────────────────────────────────

  it('should return null when no user is set', () => {
    expect(service.getCurrentUser()).toBeNull();
  });

  // ── getToken ───────────────────────────────────────────────────────────────

  it('should return token from localStorage', () => {
    localStorage.setItem('token', 'abc123');
    expect(service.getToken()).toBe('abc123');
  });

  it('should return null when no token in localStorage', () => {
    expect(service.getToken()).toBeNull();
  });

  // ── hasRole ────────────────────────────────────────────────────────────────

  it('should return false for hasRole when not logged in', () => {
    expect(service.hasRole(UserRole.ADMIN)).toBeFalse();
  });

  it('should return true for correct role after login', () => {
    const mockResponse = { token: 'tok', userId: 1, fullname: 'Admin', role: UserRole.ADMIN };
    service.login('admin@test.com', 'pass').subscribe();
    httpMock.expectOne('http://localhost:9099/auth/login').flush(mockResponse);
    httpMock.match('http://localhost:9099/auth/me').forEach(r => r.flush(mockResponse));
    expect(service.hasRole(UserRole.ADMIN)).toBeTrue();
  });

  it('should return false for wrong role', () => {
    const mockResponse = { token: 'tok', userId: 1, fullname: 'Admin', role: UserRole.ADMIN };
    service.login('admin@test.com', 'pass').subscribe();
    httpMock.expectOne('http://localhost:9099/auth/login').flush(mockResponse);
    httpMock.match('http://localhost:9099/auth/me').forEach(r => r.flush(mockResponse));
    expect(service.hasRole(UserRole.CAMPER)).toBeFalse();
  });

  // ── forgotPassword ─────────────────────────────────────────────────────────

  it('should call forgot-password endpoint', () => {
    service.forgotPassword('user@test.com').subscribe();
    const req = httpMock.expectOne('http://localhost:9099/auth/forgot-password');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'user@test.com' });
    req.flush({});
  });

  // ── resetPassword ──────────────────────────────────────────────────────────

  it('should call reset-password endpoint', () => {
    service.resetPassword('reset-token', 'newPass123').subscribe();
    const req = httpMock.expectOne('http://localhost:9099/auth/reset-password');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ token: 'reset-token', newPassword: 'newPass123' });
    req.flush({});
  });

  // ── register ───────────────────────────────────────────────────────────────

  it('should call register endpoint', () => {
    const userData = { firstName: 'John', email: 'john@test.com', password: 'pass' };
    service.register(userData).subscribe();
    const req = httpMock.expectOne('http://localhost:9099/auth/register');
    expect(req.request.method).toBe('POST');
    req.flush('OK');
  });
});
