import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { authGuard } from './auth.guard';
import { adminGuard } from './admin.guard';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models/user.model';

// ── authGuard ──────────────────────────────────────────────────────────────

describe('authGuard', () => {
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn', 'getCurrentUser', 'hasRole', 'getToken']);

    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy }
      ]
    });

    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
  });

  it('should return true when user is NOT logged in', () => {
    authServiceSpy.isLoggedIn.and.returnValue(false);
    const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));
    expect(result).toBeTrue();
  });

  it('should return false and redirect to /home when user IS logged in', () => {
    authServiceSpy.isLoggedIn.and.returnValue(true);
    const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));
    expect(result).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/home']);
  });
});

// ── adminGuard ─────────────────────────────────────────────────────────────

describe('adminGuard', () => {
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser', 'isLoggedIn', 'hasRole', 'getToken']);

    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy }
      ]
    });

    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
  });

  it('should return true for ADMIN role', () => {
    authServiceSpy.getCurrentUser.and.returnValue({ id: '1', firstName: 'Admin', lastName: '', email: '', role: UserRole.ADMIN });
    const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));
    expect(result).toBeTrue();
  });

  it('should return true for GEAR_PROVIDER role', () => {
    authServiceSpy.getCurrentUser.and.returnValue({ id: '2', firstName: 'Provider', lastName: '', email: '', role: UserRole.GEAR_PROVIDER });
    const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));
    expect(result).toBeTrue();
  });

  it('should return false and redirect for CAMPER role', () => {
    authServiceSpy.getCurrentUser.and.returnValue({ id: '3', firstName: 'Camper', lastName: '', email: '', role: UserRole.CAMPER });
    const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));
    expect(result).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/home']);
  });

  it('should return false and redirect when no user is logged in', () => {
    authServiceSpy.getCurrentUser.and.returnValue(null);
    const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));
    expect(result).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/home']);
  });
});
