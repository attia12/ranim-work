// Module: Official Campsite & Booking | Layer: Frontend Model

export type CampsiteBookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';
export type CampsitePaymentMethod = 'CARD' | 'PAYPAL' | 'BANK_TRANSFER';
export type CampsitePaymentStatus = 'PAID' | 'REFUNDED' | 'FAILED';

export interface CampsiteBookingRequest {
  campsiteId: number;
  checkInDate: string;
  checkOutDate: string;
  numberOfGuests: number;
}

export interface CampsiteBookingResponse {
  id: number;
  campsiteId: number;
  campsiteName: string;
  campsiteCountry: string;
  campsiteCity: string;
  camperId: number;
  camperFullName: string;
  checkInDate: string;
  checkOutDate: string;
  numberOfGuests: number;
  totalPrice: number;
  status: CampsiteBookingStatus;
  cancellationReason?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface BookingPage {
  content: CampsiteBookingResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface CampsitePaymentRequest {
  bookingId: number;
  amount: number;
  method: CampsitePaymentMethod;
  transactionId?: string;
  referenceCode?: string;
}

export interface CampsitePaymentResponse {
  id: number;
  bookingId: number;
  amount: number;
  transactionId?: string;
  referenceCode?: string;
  method: CampsitePaymentMethod;
  status: CampsitePaymentStatus;
  paidAt?: string;
}
