package tn.esprit.projetpidev.dto.payment;

import lombok.Data;
import tn.esprit.projetpidev.domain.enums.PaymentMethod;
import tn.esprit.projetpidev.domain.enums.PaymentStatus;

import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private Float amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}

