package tn.esprit.projetpidev.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tn.esprit.projetpidev.domain.enums.PaymentMethod;

@Data
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String transactionId;
}

