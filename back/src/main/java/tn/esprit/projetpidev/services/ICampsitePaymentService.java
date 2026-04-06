// Module: Official Campsite & Booking | Layer: Service Interface
package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.campsitepayment.CampsitePaymentRequest;
import tn.esprit.projetpidev.dto.campsitepayment.CampsitePaymentResponse;

public interface ICampsitePaymentService {

    CampsitePaymentResponse pay(CampsitePaymentRequest request);

    CampsitePaymentResponse getByBooking(Long bookingId);

    CampsitePaymentResponse getById(Long id);

    CampsitePaymentResponse refund(Long id);
}
