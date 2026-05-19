import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { CampsiteDetailComponent } from './campsite-detail.component';
import { CampsiteService } from '../../../services/campsite.service';
import { AuthService } from '../../../services/auth.service';

const mockCampsite = {
  id: 1, name: 'Forest Camp', country: 'Tunisia', city: 'Ain Draham',
  pictures: ['http://img1.jpg', 'http://img2.jpg'],
  amenities: ['WiFi', 'BBQ'], pricePerNight: 50, status: 'ACTIVE', type: 'OFFICIAL'
};

describe('CampsiteDetailComponent', () => {
  let component: CampsiteDetailComponent;
  let fixture: ComponentFixture<CampsiteDetailComponent>;
  let campsiteService: jasmine.SpyObj<CampsiteService>;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    campsiteService = jasmine.createSpyObj('CampsiteService', ['getCampsiteById']);
    authService     = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
    campsiteService.getCampsiteById.and.returnValue(of(mockCampsite as any));

    await TestBed.configureTestingModule({
      declarations: [CampsiteDetailComponent],
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [
        { provide: CampsiteService, useValue: campsiteService },
        { provide: AuthService,     useValue: authService },
        { provide: ActivatedRoute,  useValue: { snapshot: { paramMap: { get: () => '1' } } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CampsiteDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads campsite on init', () => {
    expect(campsiteService.getCampsiteById).toHaveBeenCalledWith(1);
    expect(component.campsite).toEqual(mockCampsite as any);
    expect(component.loading).toBeFalse();
  });

  it('sets error message when load fails', async () => {
    campsiteService.getCampsiteById.and.returnValue(throwError(() => new Error('Not Found')));
    component.ngOnInit();
    expect(component.error).toBe('Campsite not found.');
    expect(component.loading).toBeFalse();
  });

  it('setActiveImage() updates activeImageIndex', () => {
    component.setActiveImage(1);
    expect(component.activeImageIndex).toBe(1);
  });

  it('activeImage returns correct picture by index', () => {
    component.campsite = mockCampsite as any;
    component.activeImageIndex = 1;
    expect(component.activeImage).toBe('http://img2.jpg');
  });

  it('activeImage returns placeholder when no pictures', () => {
    component.campsite = { ...mockCampsite, pictures: [] } as any;
    expect(component.activeImage).toBe('assets/images/campsite-placeholder.jpg');
  });

  it('bookNow() navigates to /login when not logged in', () => {
    authService.isLoggedIn.and.returnValue(false);
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    component.bookNow();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('bookNow() navigates to booking page when logged in', () => {
    authService.isLoggedIn.and.returnValue(true);
    component.campsite = mockCampsite as any;
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    component.bookNow();
    expect(router.navigate).toHaveBeenCalledWith(['/campsites', 1, 'book']);
  });
});
