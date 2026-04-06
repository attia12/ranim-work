import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { MainLayoutComponent } from './main-layout.component';
import { AuthService } from '../../services/auth.service'; // ✅ 2 niveaux
import { DeliveryService } from '../../services/delivery.service'; // ✅ 2 niveaux
import { of } from 'rxjs';

describe('MainLayoutComponent', () => {
  let component: MainLayoutComponent;
  let fixture: ComponentFixture<MainLayoutComponent>;

  const mockAuthService = {
    currentUser$: of(null)
  };

  const mockDeliveryService = {
    startPolling: jasmine.createSpy('startPolling'),
    stopPolling: jasmine.createSpy('stopPolling')
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [MainLayoutComponent],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: DeliveryService, useValue: mockDeliveryService }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(MainLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});