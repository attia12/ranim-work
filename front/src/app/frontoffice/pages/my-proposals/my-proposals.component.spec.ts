import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { MyProposalsComponent } from './my-proposals.component';
import { OutdoorCampsiteService } from '../../../services/outdoor-campsite.service';

const mockProposals = [
  { id: 1, name: 'Wild Camp', status: 'PENDING' },
  { id: 2, name: 'Sea View', status: 'APPROVED' },
  { id: 3, name: 'Rocky Road', status: 'REJECTED' }
];

describe('MyProposalsComponent', () => {
  let component: MyProposalsComponent;
  let fixture: ComponentFixture<MyProposalsComponent>;
  let outdoorService: jasmine.SpyObj<OutdoorCampsiteService>;

  beforeEach(async () => {
    outdoorService = jasmine.createSpyObj('OutdoorCampsiteService', ['getMyProposals']);
    outdoorService.getMyProposals.and.returnValue(
      of({ content: mockProposals, totalPages: 1, number: 0, totalElements: 3 } as any)
    );

    await TestBed.configureTestingModule({
      declarations: [MyProposalsComponent],
      providers: [
        { provide: OutdoorCampsiteService, useValue: outdoorService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MyProposalsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads proposals on init', () => {
    expect(outdoorService.getMyProposals).toHaveBeenCalledWith(0, 50);
  });

  it('populates proposals list', () => {
    expect(component.proposals.length).toBe(3);
    expect(component.proposals[0].name).toBe('Wild Camp');
  });

  it('loading is false after data loads', () => {
    expect(component.loading).toBeFalse();
  });

  it('sets error and stops loading on failure', () => {
    outdoorService.getMyProposals.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    expect(component.error).toBe('Failed to load proposals.');
    expect(component.loading).toBeFalse();
  });

  it('statusBadgeClass() returns correct badge for PENDING', () => {
    expect(component.statusBadgeClass('PENDING')).toBe('badge-warning');
  });

  it('statusBadgeClass() returns correct badge for APPROVED', () => {
    expect(component.statusBadgeClass('APPROVED')).toBe('badge-success');
  });

  it('statusBadgeClass() returns correct badge for REJECTED', () => {
    expect(component.statusBadgeClass('REJECTED')).toBe('badge-danger');
  });

  it('statusBadgeClass() returns correct badge for SUSPENDED', () => {
    expect(component.statusBadgeClass('SUSPENDED')).toBe('badge-secondary');
  });

  it('statusBadgeClass() returns badge-light for unknown status', () => {
    expect(component.statusBadgeClass('UNKNOWN')).toBe('badge-light');
  });
});
