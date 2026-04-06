// Module: Official Campsite & Booking | Layer: Frontend Model
// NOTE: Extended to support full API fields; legacy mock fields kept for backwards compat.

export type CampsiteType = 'OFFICIAL' | 'OUTDOOR';
export type CampsiteStatus = 'ACTIVE' | 'SUSPENDED' | 'DELETED';

export interface Campsite {
  // Legacy fields (kept for mock compatibility)
  id: string;
  name: string;
  description: string;
  location: string;
  pricePerNight: number;
  imageUrl?: string;
  hasOwner?: boolean;
  ownerId?: string;
  rating?: number;
  amenities: string[];
  maxCapacity: number;

  // New API fields
  country?: string;
  city?: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  capacity?: number;
  type?: CampsiteType;
  pictures?: string[];
  rules?: string;
  status?: CampsiteStatus;
  ownerName?: string;
  createdAt?: string;
  updatedAt?: string;
}

/** API-native shape returned by /api/v1/campsites */
export interface CampsiteApiResponse {
  id: number;
  name: string;
  description: string;
  country: string;
  city: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  capacity: number;
  type: CampsiteType;
  pricePerNight: number;
  pictures: string[];
  amenities: string[];
  rules?: string;
  status: CampsiteStatus;
  ownerId?: number;
  ownerName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CampsiteRequest {
  name: string;
  description?: string;
  country: string;
  city: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  capacity: number;
  type: CampsiteType;
  pricePerNight: number;
  pictures?: string;
  amenities?: string;
  rules?: string;
}

export interface CampsitePage {
  content: CampsiteApiResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
