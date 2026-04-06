// Module: Outdoor Campsite & Booking | Layer: Frontend Component (Smart)
import { Component, OnInit } from '@angular/core';
import { OutdoorCampsiteService } from '../../../services/outdoor-campsite.service';
import { OutdoorCampsiteResponse } from '../../../models/outdoor-campsite.model';

@Component({
  selector: 'app-my-proposals',
  templateUrl: './my-proposals.component.html',
  styleUrl: './my-proposals.component.css'
})
export class MyProposalsComponent implements OnInit {

  proposals: OutdoorCampsiteResponse[] = [];
  loading = false;
  error = '';

  constructor(private outdoorService: OutdoorCampsiteService) {}

  ngOnInit(): void {
    this.loading = true;
    this.outdoorService.getMyProposals(0, 50).subscribe({
      next: (data) => { this.proposals = data.content; this.loading = false; },
      error: () => { this.error = 'Failed to load proposals.'; this.loading = false; }
    });
  }

  statusBadgeClass(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'badge-warning', APPROVED: 'badge-success',
      REJECTED: 'badge-danger', SUSPENDED: 'badge-secondary'
    };
    return map[status] || 'badge-light';
  }
}
