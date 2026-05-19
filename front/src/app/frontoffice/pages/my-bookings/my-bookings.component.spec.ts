import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { MyBookingsComponent } from './my-bookings.component';
import { CampsiteBookingService } from '../../../services/campsite-booking.service';
import { OutdoorCampsiteService } from '../../../services/outdoor-campsite.service';

const makeBookingPage = (content: any[]) => ({ content, totalPages: 1, number: 0, totalElements: content.length });

const officialBooking = { id: 1, campsiteType: 'OFFICIAL', status: 'CONFIRMED', checkInDate: '2030-08-01' };
const outdoorCampsiteBooking = { id: 2, campsiteType: 'OUTDOOR', status: 'CONFIRMED', checkInDate: '2030-08-10' };
const outdoorBooking = { id: 3, status: 'CONFIRMED', checkInDate: '2030-09-01' };

describe('MyBookingsComponent', () => {
  let component: MyBookingsComponent;
  let fixture: ComponentFixture<MyBookingsComponent>;
  let bookingService: jasmine.SpyObj<CampsiteBookingService>;
  let outdoorService: jasmine.SpyObj<OutdoorCampsiteService>;

  beforeEach(async () => {
    bookingService = jasmine.createSpyObj('CampsiteBookingService', ['getMyBookings', 'cancel']);
    outdoorService = jasmine.createSpyObj('OutdoorCampsiteService', ['getMyOutdoorBookings', 'cancelBooking']);

    bookingService.getMyBookings.and.returnValue(
      of(makeBookingPage([officialBooking, outdoorCampsiteBooking]) as any)
    );
    outdoorService.getMyOutdoorBookings.and.returnValue(
      of(makeBookingPage([outdoorBooking]) as any)
    );

    await TestBed.configureTestingModule({
      declarations: [MyBookingsComponent],
      providers: [
        { provide: CampsiteBookingService, useValue: bookingService },
        { provide: OutdoorCampsiteService, useValue: outdoorService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MyBookingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads official and outdoor bookings on init', () => {
    expect(bookingService.getMyBookings).toHaveBeenCalledWith(0, 50);
    expect(outdoorService.getMyOutdoorBookings).toHaveBeenCalledWith(0, 50);
  });

  it('separates official and outdoor campsite bookings', () => {
    expect(component.officialBookings.length).toBe(1);
    expect(component.officialBookings[0].id).toBe(1);
    expect(component.outdoorCampsiteBookings.length).toBe(1);
    expect(component.outdoorCampsiteBookings[0].id).toBe(2);
  });

  it('populates outdoorBookings from outdoor service', () => {
    expect(component.outdoorBookings.length).toBe(1);
    expect(component.outdoorBookings[0].id).toBe(3);
  });

  it('loading is false after data loads', () => {
    expect(component.loading).toBeFalse();
  });

  it('openCancelModal() sets selectedBookingId and shows modal', () => {
    component.openCancelModal(1);
    expect(component.selectedBookingId).toBe(1);
    expect(component.showCancelModal).toBeTrue();
    expect(component.cancelReason).toBe('');
  });

  it('confirmCancel() calls bookingService.cancel() and updates list', () => {
    const updated = { ...officialBooking, status: 'CANCELLED' };
    bookingService.cancel.and.returnValue(of(updated as any));
    component.officialBookings = [{ ...officialBooking } as any];
    component.openCancelModal(1);
    component.confirmCancel();
    expect(bookingService.cancel).toHaveBeenCalledWith(1, '');
    expect(component.officialBookings[0].status).toBe('CANCELLED');
    expect(component.cancellingId).toBeNull();
  });

  it('confirmCancel() does nothing when no selectedBookingId', () => {
    component.selectedBookingId = null;
    component.confirmCancel();
    expect(bookingService.cancel).not.toHaveBeenCalled();
  });

  it('canCancel() returns true for future dates beyond 2 days', () => {
    expect(component.canCancel('2099-01-01')).toBeTrue();
  });

  it('canCancel() returns false for dates within 2 days', () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    expect(component.canCancel(tomorrow.toISOString().split('T')[0])).toBeFalse();
  });

  it('badgeClass() returns correct class for each status', () => {
    expect(component.badgeClass('PENDING')).toBe('badge-warning');
    expect(component.badgeClass('CONFIRMED')).toBe('badge-success');
    expect(component.badgeClass('CANCELLED')).toBe('badge-danger');
    expect(component.badgeClass('COMPLETED')).toBe('badge-secondary');
    expect(component.badgeClass('UNKNOWN')).toBe('badge-light');
  });

  it('activeTab defaults to official', () => {
    expect(component.activeTab).toBe('official');
  });

  it('loading is false after bookingService error', () => {
    bookingService.getMyBookings.and.returnValue(throwError(() => new Error('fail')));
    component.loadBookings();
    expect(component.loading).toBeFalse();
  });
});
