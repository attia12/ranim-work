// Module: Outdoor Campsite & Booking | Layer: Frontend Component (Admin)
import { Component, OnInit } from '@angular/core';
import { OutdoorCampsiteService } from '../../services/outdoor-campsite.service';
import { OutdoorCampsiteResponse, ModerationRequest } from '../../models/outdoor-campsite.model';

@Component({
  selector: 'app-admin-outdoor-moderation',
  templateUrl: './admin-outdoor-moderation.component.html',
  styleUrl: './admin-outdoor-moderation.component.css'
})
export class AdminOutdoorModerationComponent implements OnInit {

  pendingSites: OutdoorCampsiteResponse[] = [];
  loading = false;
  processingId: number | null = null;

  showRejectModal = false;
  selectedId: number | null = null;
  rejectNote = '';

  error = '';
  success = '';

  constructor(private outdoorService: OutdoorCampsiteService) {}

  ngOnInit(): void {
    this.loadPending();
  }

  loadPending(): void {
    this.loading = true;
    this.outdoorService.getPending(0, 50).subscribe({
      next: (data) => { this.pendingSites = data.content; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  approve(id: number): void {
    this.processingId = id;
    const req: ModerationRequest = { action: 'APPROVE' };
    this.outdoorService.moderate(id, req).subscribe({
      next: () => {
        this.pendingSites = this.pendingSites.filter(s => s.id !== id);
        this.processingId = null;
        this.success = 'Campsite approved and proposer notified.';
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.processingId = null;
        this.error = err.error?.error || 'Approval failed.';
      }
    });
  }

  openRejectModal(id: number): void {
    this.selectedId = id;
    this.rejectNote = '';
    this.showRejectModal = true;
  }

  confirmReject(): void {
    if (!this.selectedId) return;
    this.processingId = this.selectedId;
    this.showRejectModal = false;
    const req: ModerationRequest = { action: 'REJECT', adminNote: this.rejectNote };
    this.outdoorService.moderate(this.selectedId, req).subscribe({
      next: () => {
        this.pendingSites = this.pendingSites.filter(s => s.id !== this.selectedId);
        this.processingId = null;
        this.success = 'Proposal rejected.';
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.processingId = null;
        this.error = err.error?.error || 'Rejection failed.';
      }
    });
  }

  difficultyBadge(diff: string | undefined): string {
    const map: Record<string, string> = { EASY: 'badge-success', MODERATE: 'badge-warning', HARD: 'badge-danger' };
    return map[diff || ''] || 'badge-secondary';
  }
}
