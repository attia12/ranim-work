import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { OutdoorCampsiteDetailComponent } from './outdoor-campsite-detail.component';
import { OutdoorCampsiteService } from '../../../services/outdoor-campsite.service';
import { AuthService } from '../../../services/auth.service';

const mockSite = {
  id: 3, name: 'Wild Valley', country: 'Tunisia', city: 'Beja',
  pictures: ['http://img1.jpg', 'http://img2.jpg'],
  naturalFeatures: ['FOREST', 'RIVER'],
  accessDifficulty: 'MODERATE', status: 'APPROVED'
};

describe('OutdoorCampsiteDetailComponent', () => {
  let component: OutdoorCampsiteDetailComponent;
  let fixture: ComponentFixture<OutdoorCampsiteDetailComponent>;
  let outdoorService: jasmine.SpyObj<OutdoorCampsiteService>;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    outdoorService = jasmine.createSpyObj('OutdoorCampsiteService', ['getById']);
    authService    = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
    outdoorService.getById.and.returnValue(of(mockSite as any));

    await TestBed.configureTestingModule({
      declarations: [OutdoorCampsiteDetailComponent],
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [
        { provide: OutdoorCampsiteService, useValue: outdoorService },
        { provide: AuthService,            useValue: authService },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '3' } } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(OutdoorCampsiteDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads outdoor campsite on init', () => {
    expect(outdoorService.getById).toHaveBeenCalledWith(3);
    expect(component.site).toEqual(mockSite as any);
    expect(component.loading).toBeFalse();
  });

  it('sets error when load fails', () => {
    outdoorService.getById.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    expect(component.error).toBe('Outdoor campsite not found.');
    expect(component.loading).toBeFalse();
  });

  it('setActiveImage() updates activeImageIndex', () => {
    component.setActiveImage(1);
    expect(component.activeImageIndex).toBe(1);
  });

  it('activeImage returns correct picture by index', () => {
    component.site = mockSite as any;
    component.activeImageIndex = 1;
    expect(component.activeImage).toBe('http://img2.jpg');
  });

  it('activeImage returns placeholder when no pictures', () => {
    component.site = { ...mockSite, pictures: [] } as any;
    expect(component.activeImage).toBe('assets/images/outdoor-placeholder.jpg');
  });

  it('difficultyBadge() returns correct class', () => {
    expect(component.difficultyBadge('EASY')).toBe('badge-success');
    expect(component.difficultyBadge('MODERATE')).toBe('badge-warning');
    expect(component.difficultyBadge('HARD')).toBe('badge-danger');
    expect(component.difficultyBadge(undefined)).toBe('badge-secondary');
  });

  it('bookNow() redirects to login when not authenticated', () => {
    authService.isLoggedIn.and.returnValue(false);
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    component.bookNow();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('bookNow() navigates to booking page when authenticated', () => {
    authService.isLoggedIn.and.returnValue(true);
    component.site = mockSite as any;
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    component.bookNow();
    expect(router.navigate).toHaveBeenCalledWith(['/outdoor-campsites', 3, 'book']);
  });
});
