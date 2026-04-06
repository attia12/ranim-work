package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.dto.payment.PaymentRequest;
import tn.esprit.projetpidev.dto.payment.PaymentResponse;

public interface IPaymentService {
    PaymentResponse createPayment(PaymentRequest request, User requester);

    PaymentResponse completePayment(Long paymentId);

    PaymentResponse refundPayment(Long paymentId);

    PaymentResponse getPaymentById(Long id);

    java.util.List<PaymentResponse> getAllPayments();
}
