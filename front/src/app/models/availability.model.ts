// Module: Official Campsite & Booking | Layer: Frontend Model

export interface AvailabilityResponse {
  id: number;
  campsiteId: number;
  campsiteName: string;
  startDate: string;
  endDate: string;
  numberOfPlaces: number;
  weatherCondition?: string;
  isBlocked: boolean;
  createdAt?: string;
}

export interface AvailabilityRequest {
  campsiteId: number;
  startDate: string;
  endDate: string;
  numberOfPlaces: number;
  weatherCondition?: string;
  isBlocked: boolean;
}
