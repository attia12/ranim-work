import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';

import { CampsiteBookingComponent } from './campsite-booking.component';
import { CampsiteService } from '../../../services/campsite.service';
import { CampsiteBookingService } from '../../../services/campsite-booking.service';

const mockCampsite = {
  id: 1, name: 'Forest Camp', pricePerNight: 50, type: 'OFFICIAL',
  capacity: 20, country: 'Tunisia', city: 'Ain Draham', status: 'ACTIVE'
};

const mockBookingResp = { id: 10, campsiteId: 1, status: 'PENDING', totalPrice: 150 };

describe('CampsiteBookingComponent', () => {
  let component: CampsiteBookingComponent;
  let fixture: ComponentFixture<CampsiteBookingComponent>;
  let campsiteService: jasmine.SpyObj<CampsiteService>;
  let bookingService: jasmine.SpyObj<CampsiteBookingService>;

  beforeEach(async () => {
    campsiteService = jasmine.createSpyObj('CampsiteService', ['getCampsiteById']);
    bookingService  = jasmine.createSpyObj('CampsiteBookingService', ['create']);
    campsiteService.getCampsiteById.and.returnValue(of(mockCampsite as any));

    await TestBed.configureTestingModule({
      declarations: [CampsiteBookingComponent],
      imports: [HttpClientTestingModule, RouterTestingModule, ReactiveFormsModule],
      providers: [
        { provide: CampsiteService,        useValue: campsiteService },
        { provide: CampsiteBookingService, useValue: bookingService },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '1' } } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CampsiteBookingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads campsite on init', () => {
    expect(campsiteService.getCampsiteById).toHaveBeenCalledWith(1);
    expect(component.campsite).toEqual(mockCampsite as any);
  });

  it('sets error when campsite load fails', () => {
    campsiteService.getCampsiteById.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    expect(component.error).toBe('Campsite not found.');
  });

  it('form is invalid when required fields missing', () => {
    expect(component.bookingForm.valid).toBeFalse();
  });

  it('form is valid with all required fields', () => {
    component.bookingForm.setValue({
      checkInDate: '2026-07-01', checkOutDate: '2026-07-05', numberOfGuests: 2
    });
    expect(component.bookingForm.valid).toBeTrue();
  });

  it('nights computed correctly', () => {
    component.bookingForm.setValue({
      checkInDate: '2026-07-01', checkOutDate: '2026-07-05', numberOfGuests: 2
    });
    expect(component.nights).toBe(4);
  });

  it('nights returns 0 when dates not set', () => {
    expect(component.nights).toBe(0);
  });

  it('totalPrice computed as nights x pricePerNight x guests', () => {
    component.campsite = mockCampsite as any;
    component.bookingForm.setValue({
      checkInDate: '2026-07-01', checkOutDate: '2026-07-05', numberOfGuests: 2
    });
    // 4 nights x 50 x 2 guests = 400
    expect(component.totalPrice).toBe(400);
  });

  it('totalPrice is 0 for OUTDOOR type', () => {
    component.campsite = { ...mockCampsite, type: 'OUTDOOR' } as any;
    component.bookingForm.setValue({
      checkInDate: '2026-07-01', checkOutDate: '2026-07-05', numberOfGuests: 2
    });
    expect(component.totalPrice).toBe(0);
  });

  it('submit() does nothing when form invalid', () => {
    component.submit();
    expect(bookingService.create).not.toHaveBeenCalled();
  });

  it('submit() calls bookingService.create() with form data', () => {
    bookingService.create.and.returnValue(of(mockBookingResp as any));
    component.bookingForm.setValue({
      checkInDate: '2026-07-01', checkOutDate: '2026-07-05', numberOfGuests: 2
    });
    component.submit();
    expect(bookingService.create).toHaveBeenCalledWith(jasmine.objectContaining({
      campsiteId: 1, checkInDate: '2026-07-01', checkOutDate: '2026-07-05', numberOfGuests: 2
    }));
  });

  it('submit() sets error on service failure', () => {
    bookingService.create.and.returnValue(throwError(() => ({ error: { message: 'Booking failed' } })));
    component.bookingForm.setValue({
      checkInDate: '2026-07-01', checkOutDate: '2026-07-05', numberOfGuests: 2
    });
    component.submit();
    expect(component.error).toBe('Booking failed');
  });
});
