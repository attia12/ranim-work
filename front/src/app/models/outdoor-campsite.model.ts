// Module: Outdoor Campsite & Booking | Layer: Frontend Model

export type AccessDifficulty = 'EASY' | 'MODERATE' | 'HARD';
export type OutdoorCampsiteStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUSPENDED';
export type OutdoorBookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED';

export interface OutdoorCampsiteResponse {
  id: number;
  name: string;
  description?: string;
  country: string;
  city: string;
  latitude?: number;
  longitude?: number;
  pictures: string[];
  naturalFeatures: string[];
  accessDifficulty?: AccessDifficulty;
  proposedById: number;
  proposedByName: string;
  status: OutdoorCampsiteStatus;
  adminNote?: string;
  approvedById?: number;
  approvedByName?: string;
  approvedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface OutdoorCampsiteRequest {
  name: string;
  description?: string;
  country: string;
  city: string;
  latitude?: number;
  longitude?: number;
  pictures?: string;
  naturalFeatures?: string;
  accessDifficulty?: AccessDifficulty;
}

export interface OutdoorCampsitePage {
  content: OutdoorCampsiteResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface OutdoorBookingRequest {
  outdoorCampsiteId: number;
  checkInDate: string;
  checkOutDate: string;
  numberOfGuests: number;
}

export interface OutdoorBookingResponse {
  id: number;
  outdoorCampsiteId: number;
  outdoorCampsiteName: string;
  outdoorCampsiteCountry: string;
  outdoorCampsiteCity: string;
  camperId: number;
  camperFullName: string;
  checkInDate: string;
  checkOutDate: string;
  numberOfGuests: number;
  status: OutdoorBookingStatus;
  createdAt?: string;
  updatedAt?: string;
}

export interface OutdoorBookingPage {
  content: OutdoorBookingResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface ModerationRequest {
  action: 'APPROVE' | 'REJECT' | 'SUSPEND';
  adminNote?: string;
}
