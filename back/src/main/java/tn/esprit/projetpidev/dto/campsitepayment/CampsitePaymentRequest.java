// Module: Official Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.campsitepayment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tn.esprit.projetpidev.domain.enums.CampsitePaymentMethod;

import java.math.BigDecimal;

@Data
public class CampsitePaymentRequest {

    @NotNull
    private Long bookingId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private CampsitePaymentMethod method;

    private String transactionId;
    private String referenceCode;
}
