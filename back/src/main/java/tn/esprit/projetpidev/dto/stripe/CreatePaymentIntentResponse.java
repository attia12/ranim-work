package tn.esprit.projetpidev.dto.stripe;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePaymentIntentResponse {

    private String clientSecret;
    private String publishableKey;
    private String paymentIntentId;
}
