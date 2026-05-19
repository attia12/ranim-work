import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { OutdoorBookingComponent } from './outdoor-booking.component';
import { OutdoorCampsiteService } from '../../../services/outdoor-campsite.service';

const mockSite = {
  id: 3, name: 'Wild Valley', status: 'APPROVED',
  country: 'Tunisia', city: 'Beja', accessDifficulty: 'EASY',
  pictures: ['http://img1.jpg'], naturalFeatures: ['FOREST'],
  description: 'A wild valley'
};
const mockBookingResp = { id: 1, status: 'CONFIRMED' };

describe('OutdoorBookingComponent', () => {
  let component: OutdoorBookingComponent;
  let fixture: ComponentFixture<OutdoorBookingComponent>;
  let outdoorService: jasmine.SpyObj<OutdoorCampsiteService>;

  beforeEach(async () => {
    outdoorService = jasmine.createSpyObj('OutdoorCampsiteService', ['getById', 'book']);
    outdoorService.getById.and.returnValue(of(mockSite as any));

    await TestBed.configureTestingModule({
      declarations: [OutdoorBookingComponent],
      imports: [RouterTestingModule, ReactiveFormsModule],
      providers: [
        { provide: OutdoorCampsiteService, useValue: outdoorService },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '3' } } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(OutdoorBookingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads outdoor site on init', () => {
    expect(outdoorService.getById).toHaveBeenCalledWith(3);
    expect(component.site).toEqual(mockSite as any);
  });

  it('sets error when site load fails', () => {
    outdoorService.getById.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    expect(component.error).toBe('Outdoor campsite not found.');
  });

  it('form initializes with default values', () => {
    expect(component.bookingForm.get('numberOfGuests')?.value).toBe(1);
  });

  it('form is invalid when required fields missing', () => {
    expect(component.bookingForm.valid).toBeFalse();
  });

  it('form is valid with all required fields', () => {
    component.bookingForm.setValue({
      checkInDate: '2026-08-01', checkOutDate: '2026-08-05', numberOfGuests: 3
    });
    expect(component.bookingForm.valid).toBeTrue();
  });

  it('nights computed correctly', () => {
    component.bookingForm.setValue({
      checkInDate: '2026-08-01', checkOutDate: '2026-08-05', numberOfGuests: 2
    });
    expect(component.nights).toBe(4);
  });

  it('nights returns 0 when dates not set', () => {
    expect(component.nights).toBe(0);
  });

  it('submit() does nothing when form invalid', () => {
    component.submit();
    expect(outdoorService.book).not.toHaveBeenCalled();
  });

  it('submit() calls outdoorService.book() with correct data', () => {
    outdoorService.book.and.returnValue(of(mockBookingResp as any));
    component.bookingForm.setValue({
      checkInDate: '2026-08-01', checkOutDate: '2026-08-05', numberOfGuests: 2
    });
    component.submit();
    expect(outdoorService.book).toHaveBeenCalledWith({
      outdoorCampsiteId: 3,
      checkInDate: '2026-08-01',
      checkOutDate: '2026-08-05',
      numberOfGuests: 2
    });
  });

  it('submit() navigates to /my-bookings on success', () => {
    outdoorService.book.and.returnValue(of(mockBookingResp as any));
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    component.bookingForm.setValue({
      checkInDate: '2026-08-01', checkOutDate: '2026-08-05', numberOfGuests: 2
    });
    component.submit();
    expect(router.navigate).toHaveBeenCalledWith(['/my-bookings']);
  });

  it('submit() sets error on failure', () => {
    outdoorService.book.and.returnValue(throwError(() => ({ error: { message: 'No availability' } })));
    component.bookingForm.setValue({
      checkInDate: '2026-08-01', checkOutDate: '2026-08-05', numberOfGuests: 2
    });
    component.submit();
    expect(component.error).toBe('No availability');
    expect(component.submitting).toBeFalse();
  });
});
