import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { AdminOutdoorModerationComponent } from './admin-outdoor-moderation.component';
import { OutdoorCampsiteService } from '../../services/outdoor-campsite.service';

const mockSites = [
  { id: 1, name: 'Wild Valley', status: 'PENDING', accessDifficulty: 'EASY' },
  { id: 2, name: 'Rocky Peak',  status: 'PENDING', accessDifficulty: 'HARD' }
];

describe('AdminOutdoorModerationComponent', () => {
  let component: AdminOutdoorModerationComponent;
  let fixture: ComponentFixture<AdminOutdoorModerationComponent>;
  let outdoorService: jasmine.SpyObj<OutdoorCampsiteService>;

  beforeEach(async () => {
    outdoorService = jasmine.createSpyObj('OutdoorCampsiteService', ['getPending', 'moderate']);
    outdoorService.getPending.and.returnValue(
      of({ content: [...mockSites], totalPages: 1, number: 0, totalElements: 2 } as any)
    );

    await TestBed.configureTestingModule({
      declarations: [AdminOutdoorModerationComponent],
      providers: [
        { provide: OutdoorCampsiteService, useValue: outdoorService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminOutdoorModerationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads pending sites on init', () => {
    expect(outdoorService.getPending).toHaveBeenCalledWith(0, 50);
    expect(component.pendingSites.length).toBe(2);
  });

  it('loading is false after data loads', () => {
    expect(component.loading).toBeFalse();
  });

  it('approve() calls moderate with APPROVE action', () => {
    outdoorService.moderate.and.returnValue(of({ id: 1, status: 'APPROVED' } as any));
    component.approve(1);
    expect(outdoorService.moderate).toHaveBeenCalledWith(1, { action: 'APPROVE' });
  });

  it('approve() removes site from pendingSites list on success', () => {
    outdoorService.moderate.and.returnValue(of({ id: 1, status: 'APPROVED' } as any));
    component.approve(1);
    expect(component.pendingSites.find(s => s.id === 1)).toBeUndefined();
    expect(component.pendingSites.length).toBe(1);
  });

  it('approve() sets success message on success', () => {
    outdoorService.moderate.and.returnValue(of({ id: 1, status: 'APPROVED' } as any));
    component.approve(1);
    expect(component.success).toBe('Campsite approved and proposer notified.');
    expect(component.processingId).toBeNull();
  });

  it('approve() sets error on failure', () => {
    outdoorService.moderate.and.returnValue(
      throwError(() => ({ error: { error: 'Approval failed' } }))
    );
    component.approve(1);
    expect(component.error).toBe('Approval failed');
    expect(component.processingId).toBeNull();
  });

  it('openRejectModal() sets selectedId and shows modal', () => {
    component.openRejectModal(2);
    expect(component.selectedId).toBe(2);
    expect(component.showRejectModal).toBeTrue();
    expect(component.rejectNote).toBe('');
  });

  it('confirmReject() does nothing when no selectedId', () => {
    component.selectedId = null;
    component.confirmReject();
    expect(outdoorService.moderate).not.toHaveBeenCalled();
  });

  it('confirmReject() calls moderate with REJECT action and note', () => {
    outdoorService.moderate.and.returnValue(of({ id: 2, status: 'REJECTED' } as any));
    component.selectedId = 2;
    component.rejectNote = 'Too dangerous';
    component.confirmReject();
    expect(outdoorService.moderate).toHaveBeenCalledWith(2, { action: 'REJECT', adminNote: 'Too dangerous' });
  });

  it('confirmReject() removes site from list on success', () => {
    outdoorService.moderate.and.returnValue(of({ id: 2, status: 'REJECTED' } as any));
    component.selectedId = 2;
    component.confirmReject();
    expect(component.pendingSites.find(s => s.id === 2)).toBeUndefined();
  });

  it('confirmReject() hides modal before calling service', () => {
    outdoorService.moderate.and.returnValue(of({ id: 2, status: 'REJECTED' } as any));
    component.selectedId = 2;
    component.showRejectModal = true;
    component.confirmReject();
    expect(component.showRejectModal).toBeFalse();
  });

  it('difficultyBadge() returns correct badge class', () => {
    expect(component.difficultyBadge('EASY')).toBe('badge-success');
    expect(component.difficultyBadge('MODERATE')).toBe('badge-warning');
    expect(component.difficultyBadge('HARD')).toBe('badge-danger');
    expect(component.difficultyBadge(undefined)).toBe('badge-secondary');
  });
});
