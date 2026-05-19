import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';

import { CampsiteOwnerComponent } from './campsite-owner.component';
import { CampsiteService } from '../../services/campsite.service';
import { CampsiteBookingService } from '../../services/campsite-booking.service';
import { AuthService } from '../../services/auth.service';

const mockCampsite = {
  id: 1, name: 'Forest Camp', country: 'Tunisia', city: 'Ain Draham',
  pictures: ['img1.jpg'], amenities: ['BBQ'], pricePerNight: 50,
  status: 'ACTIVE', type: 'OFFICIAL', naturalFeatures: ['FOREST']
};
const mockPage = { content: [mockCampsite], totalPages: 1, number: 0, totalElements: 1 };
const mockBookingPage = { content: [], totalPages: 0, number: 0, totalElements: 0 };

describe('CampsiteOwnerComponent', () => {
  let component: CampsiteOwnerComponent;
  let fixture: ComponentFixture<CampsiteOwnerComponent>;
  let campsiteService: jasmine.SpyObj<CampsiteService>;
  let bookingService: jasmine.SpyObj<CampsiteBookingService>;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    campsiteService = jasmine.createSpyObj('CampsiteService', [
      'getMyCampsites', 'getAllAdmin', 'create', 'update', 'delete',
      'getStatusHistory', 'refreshStatus'
    ]);
    bookingService  = jasmine.createSpyObj('CampsiteBookingService', ['getByCampsite', 'confirm']);
    authService     = jasmine.createSpyObj('AuthService', ['hasRole']);

    authService.hasRole.and.returnValue(false);
    campsiteService.getMyCampsites.and.returnValue(of(mockPage as any));
    campsiteService.getStatusHistory.and.returnValue(of([]));
    bookingService.getByCampsite.and.returnValue(of(mockBookingPage as any));

    await TestBed.configureTestingModule({
      declarations: [CampsiteOwnerComponent],
      imports: [HttpClientTestingModule, RouterTestingModule, ReactiveFormsModule],
      providers: [
        { provide: CampsiteService,        useValue: campsiteService },
        { provide: CampsiteBookingService, useValue: bookingService },
        { provide: AuthService,            useValue: authService },
        { provide: ActivatedRoute, useValue: { queryParams: of({}) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CampsiteOwnerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('isAdmin is false for non-admin user', () => {
    expect(component.isAdmin).toBeFalse();
    expect(campsiteService.getMyCampsites).toHaveBeenCalled();
  });

  it('admin user calls getAllAdmin instead of getMyCampsites', () => {
    authService.hasRole.and.returnValue(true);
    campsiteService.getAllAdmin.and.returnValue(of(mockPage as any));
    component.isAdmin = true;
    component.loadMyCampsites();
    expect(campsiteService.getAllAdmin).toHaveBeenCalled();
  });

  it('loads campsites on init', () => {
    expect(component.campsites.length).toBe(1);
    expect(component.campsites[0].id).toBe(1);
  });

  it('selectCampsite() sets selectedCampsite and loads bookings', () => {
    component.selectCampsite(mockCampsite as any);
    expect(component.selectedCampsite).toEqual(mockCampsite as any);
    expect(bookingService.getByCampsite).toHaveBeenCalledWith(1, 0, 50);
  });

  it('openCreateForm() resets form and shows it', () => {
    component.openCreateForm();
    expect(component.showCampsiteForm).toBeTrue();
    expect(component.editingCampsite).toBeNull();
  });

  it('openEditForm() patches form with campsite data', () => {
    component.openEditForm(mockCampsite as any);
    expect(component.showCampsiteForm).toBeTrue();
    expect(component.editingCampsite).toEqual(mockCampsite as any);
    expect(component.campsiteForm.get('name')?.value).toBe('Forest Camp');
  });

  it('toggleFeature() adds and removes features', () => {
    component.selectedFeatures = [];
    component.toggleFeature('FOREST');
    expect(component.selectedFeatures).toContain('FOREST');
    component.toggleFeature('FOREST');
    expect(component.selectedFeatures).not.toContain('FOREST');
  });

  it('hasFeature() reflects selectedFeatures state', () => {
    component.selectedFeatures = ['LAKE'];
    expect(component.hasFeature('LAKE')).toBeTrue();
    expect(component.hasFeature('FOREST')).toBeFalse();
  });

  it('saveCampsite() calls create when not editing', () => {
    campsiteService.create.and.returnValue(of(mockCampsite as any));
    campsiteService.getMyCampsites.and.returnValue(of(mockPage as any));
    component.editingCampsite = null;
    component.campsiteForm.patchValue({
      name: 'New Camp', country: 'Tunisia', city: 'Tunis',
      capacity: 10, type: 'OFFICIAL', pricePerNight: 30
    });
    component.saveCampsite();
    expect(campsiteService.create).toHaveBeenCalled();
  });

  it('saveCampsite() calls update when editing', () => {
    campsiteService.update.and.returnValue(of(mockCampsite as any));
    campsiteService.getMyCampsites.and.returnValue(of(mockPage as any));
    component.editingCampsite = mockCampsite as any;
    component.campsiteForm.patchValue({
      name: 'Updated', country: 'Tunisia', city: 'Tunis',
      capacity: 10, type: 'OFFICIAL', pricePerNight: 30
    });
    component.saveCampsite();
    expect(campsiteService.update).toHaveBeenCalledWith(1, jasmine.any(Object));
  });

  it('confirmBooking() calls bookingService.confirm()', () => {
    const updated = { id: 5, status: 'CONFIRMED' };
    bookingService.confirm.and.returnValue(of(updated as any));
    component.bookings = [{ id: 5, status: 'PENDING' } as any];
    component.confirmBooking(5);
    expect(bookingService.confirm).toHaveBeenCalledWith(5);
    expect(component.bookings[0].status).toBe('CONFIRMED');
  });

  it('badgeClass() returns correct class for each booking status', () => {
    expect(component.badgeClass('PENDING')).toBe('badge-warning');
    expect(component.badgeClass('CONFIRMED')).toBe('badge-success');
    expect(component.badgeClass('CANCELLED')).toBe('badge-danger');
    expect(component.badgeClass('COMPLETED')).toBe('badge-secondary');
  });

  it('statusBadgeClass() returns correct class for campsite status', () => {
    expect(component.statusBadgeClass('ACTIVE')).toBe('badge-success');
    expect(component.statusBadgeClass('SUSPENDED')).toBe('badge-secondary');
    expect(component.statusBadgeClass('DELETED')).toBe('badge-danger');
  });
});
