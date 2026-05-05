// Module: Official Campsite & Booking | Layer: Frontend Component (Smart - Owner Dashboard)
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { CampsiteService } from '../../services/campsite.service';
import { CampsiteBookingService } from '../../services/campsite-booking.service';
import { AuthService } from '../../services/auth.service';
import { CampsiteApiResponse, CampsiteRequest } from '../../models/campsite.model';
import { CampsiteBookingResponse } from '../../models/campsite-booking.model';
import { CampsiteStatusHistoryEntry } from '../../models/campsite-status.model';
import { AvailabilityResponse, AvailabilityRequest } from '../../models/availability.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-campsite-owner',
  templateUrl: './campsite-owner.component.html',
  styleUrl: './campsite-owner.component.css'
})
export class CampsiteOwnerComponent implements OnInit {

  campsites: CampsiteApiResponse[] = [];
  bookings: CampsiteBookingResponse[] = [];
  availabilities: AvailabilityResponse[] = [];
  statusHistory: CampsiteStatusHistoryEntry[] = [];
  selectedCampsite: CampsiteApiResponse | null = null;

  loading = false;
  showCampsiteForm = false;
  editingCampsite: CampsiteApiResponse | null = null;
  campsiteForm!: FormGroup;

  showAvailabilityForm = false;
  availForm!: FormGroup;

  error = '';
  successMsg = '';
  refreshing = false;

  today = new Date().toISOString().split('T')[0];
  types = ['OFFICIAL', 'OUTDOOR'];
  allFeatures = ['FOREST', 'LAKE', 'MOUNTAIN', 'BEACH', 'RIVER', 'PLAIN'];
  featureIcons: Record<string, string> = {
    FOREST: '🌲', LAKE: '🏞️', MOUNTAIN: '⛰️', BEACH: '🏖️', RIVER: '🌊', PLAIN: '🌾'
  };
  selectedFeatures: string[] = [];

  isAdmin = false;

  constructor(
    private fb: FormBuilder,
    private campsiteService: CampsiteService,
    private bookingService: CampsiteBookingService,
    private http: HttpClient,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.authService.hasRole('ADMIN');
    this.initForms();
    this.loadMyCampsites();
    this.route.queryParams.subscribe(params => {
      if (params['action'] === 'new') {
        this.openCreateForm();
      } else if (params['action'] === 'availability') {
        this.openAvailabilityForm();
      }
    });
  }

  initForms(): void {
    this.campsiteForm = this.fb.group({
      name:          ['', [Validators.required]],
      description:   [''],
      country:       ['', Validators.required],
      city:          ['', Validators.required],
      address:       [''],
      latitude:      [null],
      longitude:     [null],
      capacity:      [1, [Validators.required, Validators.min(1)]],
      type:          ['OFFICIAL', Validators.required],
      pricePerNight: [0, [Validators.required, Validators.min(0)]],
      pictures:      [''],
      amenities:     [''],
      rules:         [''],
      startDate:        [null],
      endDate:          [null],
      naturalFeatures:  ['']
    });

    this.campsiteForm.get('type')!.valueChanges.subscribe(type => {
      const priceCtrl = this.campsiteForm.get('pricePerNight')!;
      if (type === 'OUTDOOR') {
        priceCtrl.setValue(0);
        priceCtrl.clearValidators();
      } else {
        priceCtrl.setValidators([Validators.required, Validators.min(0)]);
      }
      priceCtrl.updateValueAndValidity();
    });

    this.availForm = this.fb.group({
      startDate:      ['', Validators.required],
      endDate:        ['', Validators.required],
      numberOfPlaces: [1, [Validators.required, Validators.min(0)]],
      weatherCondition: [''],
      isBlocked:      [false]
    });
  }

  loadMyCampsites(): void {
    this.loading = true;
    const req = this.isAdmin
      ? this.campsiteService.getAllAdmin(0, 200)
      : this.campsiteService.getMyCampsites(0, 50);
    req.subscribe({
      next: (data) => { this.campsites = data.content; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  selectCampsite(campsite: CampsiteApiResponse): void {
    this.selectedCampsite = campsite;
    this.loadBookings(campsite.id);
    this.loadAvailability(campsite.id);
    this.loadStatusHistory(campsite.id);
  }

  loadStatusHistory(campsiteId: number): void {
    this.campsiteService.getStatusHistory(campsiteId).subscribe({
      next: (data) => this.statusHistory = data,
      error: () => this.statusHistory = []
    });
  }

  refreshStatus(campsiteId: number): void {
    this.refreshing = true;
    this.campsiteService.refreshStatus(campsiteId).subscribe({
      next: (result: any) => {
        this.refreshing = false;
        this.successMsg = result.changed
          ? `Status updated → ${result.newStatus}: ${result.reason}`
          : `Status unchanged (${result.newStatus}): ${result.reason}`;
        setTimeout(() => this.successMsg = '', 6000);
        // reload campsite list + history to show new status
        this.loadMyCampsites();
        this.loadStatusHistory(campsiteId);
      },
      error: () => {
        this.refreshing = false;
        this.error = 'Status refresh failed. Make sure you are an admin or check backend logs.';
      }
    });
  }

  statusBadgeClass(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: 'badge-success',
      FULL: 'badge-warning',
      PENDING: 'badge-info',
      SUSPENDED: 'badge-secondary',
      EXPIRED: 'badge-dark',
      DELETED: 'badge-danger'
    };
    return map[status] || 'badge-light';
  }

  loadBookings(campsiteId: number): void {
    this.bookingService.getByCampsite(campsiteId, 0, 50).subscribe({
      next: (data) => this.bookings = data.content,
      error: () => {}
    });
  }

  loadAvailability(campsiteId: number): void {
    this.http.get<AvailabilityResponse[]>(
      `${environment.apiUrl}/api/v1/availabilities/campsite/${campsiteId}`
    ).subscribe({
      next: (data) => this.availabilities = data,
      error: () => {}
    });
  }

  private initPlacesAutocomplete(): void {
    const input = document.getElementById('location-autocomplete') as HTMLInputElement;
    if (!input || !(window as any).google?.maps?.places) return;
    const ac = new (window as any).google.maps.places.Autocomplete(input, { types: ['geocode'] });
    ac.addListener('place_changed', () => {
      const place = ac.getPlace();
      if (!place.geometry) return;
      let city = '';
      let country = '';
      for (const comp of (place.address_components || [])) {
        if (comp.types.includes('locality') || comp.types.includes('postal_town')) city = comp.long_name;
        if (comp.types.includes('country')) country = comp.long_name;
      }
      this.campsiteForm.patchValue({
        city,
        country,
        address: place.formatted_address || '',
        latitude: place.geometry.location.lat(),
        longitude: place.geometry.location.lng()
      });
    });
  }

  openCreateForm(): void {
    this.editingCampsite = null;
    this.campsiteForm.reset({
      name:          'Pine Forest Camp',
      description:   'A peaceful campsite in the pine forest.',
      country:       'Tunisia',
      city:          'Ain Draham',
      address:       'Route Forestiere km 5',
      latitude:      36.78,
      longitude:     8.69,
      capacity:      20,
      type:          'OFFICIAL',
      pricePerNight: 35.00,
      pictures:      'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=800',
      amenities:     'Toilets,Showers,BBQ,Parking',
      rules:         'No loud music after 10pm. Fires only in designated areas.'
    });
    this.selectedFeatures = [];
    this.showCampsiteForm = true;
    setTimeout(() => this.initPlacesAutocomplete(), 100);
  }

  openEditForm(campsite: CampsiteApiResponse): void {
    this.editingCampsite = campsite;
    this.selectedFeatures = campsite.naturalFeatures ? [...campsite.naturalFeatures] : [];
    this.campsiteForm.patchValue({
      ...campsite,
      pictures: campsite.pictures?.join(',') || '',
      amenities: campsite.amenities?.join(',') || '',
      startDate: campsite.startDate || null,
      endDate: campsite.endDate || null,
      naturalFeatures: this.selectedFeatures.join(',')
    });
    this.showCampsiteForm = true;
    setTimeout(() => this.initPlacesAutocomplete(), 100);
  }

  toggleFeature(feature: string): void {
    const idx = this.selectedFeatures.indexOf(feature);
    if (idx >= 0) {
      this.selectedFeatures.splice(idx, 1);
    } else {
      this.selectedFeatures.push(feature);
    }
    this.campsiteForm.get('naturalFeatures')!.setValue(this.selectedFeatures.join(','));
  }

  hasFeature(feature: string): boolean {
    return this.selectedFeatures.includes(feature);
  }

  saveCampsite(): void {
    if (this.campsiteForm.invalid) { this.campsiteForm.markAllAsTouched(); return; }
    const raw = this.campsiteForm.value;
    const req: CampsiteRequest = {
      ...raw,
      startDate: raw.startDate || null,
      endDate:   raw.endDate   || null,
    };
    const obs = this.editingCampsite
      ? this.campsiteService.update(this.editingCampsite.id, req)
      : this.campsiteService.create(req);

    obs.subscribe({
      next: () => {
        this.showCampsiteForm = false;
        this.successMsg = this.editingCampsite ? 'Campsite updated!' : 'Campsite created!';
        this.loadMyCampsites();
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: (err) => { this.error = err.error?.error || 'Save failed.'; }
    });
  }

  deleteCampsite(id: number): void {
    if (!confirm('Are you sure you want to delete this campsite?')) return;
    this.campsiteService.delete(id).subscribe({
      next: () => { this.campsites = this.campsites.filter(c => c.id !== id); },
      error: (err) => { this.error = err.error?.error || 'Delete failed.'; }
    });
  }

  openAvailabilityForm(): void {
    this.availForm.reset({
      startDate:        '2026-05-01',
      endDate:          '2026-05-31',
      numberOfPlaces:   20,
      weatherCondition: 'Sunny',
      isBlocked:        false
    });
    this.showAvailabilityForm = true;
  }

  addAvailability(): void {
    if (!this.selectedCampsite || this.availForm.invalid) {
      this.availForm.markAllAsTouched(); return;
    }
    const req: AvailabilityRequest = {
      campsiteId: this.selectedCampsite.id,
      ...this.availForm.value
    };
    this.http.post<AvailabilityResponse>(
      `${environment.apiUrl}/api/v1/availabilities`, req
    ).subscribe({
      next: (data) => {
        this.availabilities.push(data);
        this.availForm.reset({ isBlocked: false, numberOfPlaces: 1 });
        this.showAvailabilityForm = false;
      },
      error: (err) => { this.error = err.error?.error || 'Failed to add availability.'; }
    });
  }

  deleteAvailability(id: number): void {
    this.http.delete(`${environment.apiUrl}/api/v1/availabilities/${id}`).subscribe({
      next: () => { this.availabilities = this.availabilities.filter(a => a.id !== id); },
      error: () => {}
    });
  }

  confirmBooking(id: number): void {
    this.bookingService.confirm(id).subscribe({
      next: (updated) => {
        const idx = this.bookings.findIndex(b => b.id === updated.id);
        if (idx >= 0) this.bookings[idx] = updated;
      }
    });
  }

  badgeClass(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'badge-warning', CONFIRMED: 'badge-success',
      CANCELLED: 'badge-danger', COMPLETED: 'badge-secondary'
    };
    return map[status] || 'badge-light';
  }
}
