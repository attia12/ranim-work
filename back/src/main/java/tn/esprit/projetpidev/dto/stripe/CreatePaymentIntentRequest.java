package tn.esprit.projetpidev.dto.stripe;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentIntentRequest {

    @NotNull
    private Long bookingId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}
