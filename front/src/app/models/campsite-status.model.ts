// Module: Official Campsite & Booking | Layer: Frontend Model (Status)
import { CampsiteStatus } from './campsite.model';

export interface CampsiteStatusHistoryEntry {
  id: number;
  previousStatus: CampsiteStatus | null;
  newStatus: CampsiteStatus;
  reason: string;
  changedBy: string;
  changedAt: string;
}

export interface StatusPreview {
  currentStatus: CampsiteStatus;
  evaluatedStatus: CampsiteStatus;
  reason: string;
}
