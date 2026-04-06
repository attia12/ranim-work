import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from '../login/login.component';
import { ForgotPasswordComponent } from './forgot-password.component';
import { AuthService } from '../../../services/auth.service';
import { FormsModule } from '@angular/forms';

// ── LoginComponent ─────────────────────────────────────────────────────────

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [FormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with showPassword as false', () => {
    expect(component.showPassword).toBeFalse();
  });

  it('should toggle showPassword on togglePassword()', () => {
    component.togglePassword();
    expect(component.showPassword).toBeTrue();
    component.togglePassword();
    expect(component.showPassword).toBeFalse();
  });

  it('should call authService.login with correct credentials on submit', () => {
    authServiceSpy.login.and.returnValue(of({ token: 'tok' }));
    component.credentials = { email: 'test@test.com', password: 'pass123' };
    component.onSubmit();
    expect(authServiceSpy.login).toHaveBeenCalledWith('test@test.com', 'pass123');
  });

  it('should navigate to /home on successful login', () => {
    authServiceSpy.login.and.returnValue(of({ token: 'tok' }));
    component.credentials = { email: 'test@test.com', password: 'pass' };
    component.onSubmit();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/home']);
  });

  it('should not navigate on login error', () => {
    authServiceSpy.login.and.returnValue(throwError(() => new Error('Invalid credentials')));
    component.credentials = { email: 'bad@test.com', password: 'wrong' };
    component.onSubmit();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });
});

// ── ForgotPasswordComponent ────────────────────────────────────────────────

describe('ForgotPasswordComponent', () => {
  let component: ForgotPasswordComponent;
  let fixture: ComponentFixture<ForgotPasswordComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['forgotPassword']);

    await TestBed.configureTestingModule({
      declarations: [ForgotPasswordComponent],
      imports: [FormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ForgotPasswordComponent);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with submitted as false', () => {
    expect(component.submitted).toBeFalse();
  });

  it('should start with loading as false', () => {
    expect(component.loading).toBeFalse();
  });

  it('should set submitted to true on success', () => {
    authServiceSpy.forgotPassword.and.returnValue(of({}));
    component.email = 'user@test.com';
    component.onSubmit();
    expect(component.submitted).toBeTrue();
  });

  it('should set loading to false after success', () => {
    authServiceSpy.forgotPassword.and.returnValue(of({}));
    component.email = 'user@test.com';
    component.onSubmit();
    expect(component.loading).toBeFalse();
  });

  it('should set errorMsg on error', () => {
    authServiceSpy.forgotPassword.and.returnValue(
      throwError(() => ({ error: { message: 'Email not found' } }))
    );
    component.email = 'unknown@test.com';
    component.onSubmit();
    expect(component.errorMsg).toBe('Email not found');
  });

  it('should set default errorMsg when error has no message', () => {
    authServiceSpy.forgotPassword.and.returnValue(
      throwError(() => ({ error: {} }))
    );
    component.email = 'unknown@test.com';
    component.onSubmit();
    expect(component.errorMsg).toBe('An error occurred. Please try again.');
  });

  it('should set loading to false on error', () => {
    authServiceSpy.forgotPassword.and.returnValue(
      throwError(() => ({ error: {} }))
    );
    component.onSubmit();
    expect(component.loading).toBeFalse();
  });

  it('should call authService.forgotPassword with the correct email', () => {
    authServiceSpy.forgotPassword.and.returnValue(of({}));
    component.email = 'myemail@test.com';
    component.onSubmit();
    expect(authServiceSpy.forgotPassword).toHaveBeenCalledWith('myemail@test.com');
  });
});
