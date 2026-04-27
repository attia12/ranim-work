package tn.esprit.projetpidev.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.esprit.projetpidev.dto.stripe.CreatePaymentIntentRequest;
import tn.esprit.projetpidev.dto.stripe.CreatePaymentIntentResponse;

import java.math.BigDecimal;

@Slf4j
@Service
public class StripeService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    /**
     * Creates a Stripe PaymentIntent and returns the clientSecret the frontend uses
     * to confirm the payment with Stripe.js.
     * Amount is treated as EUR cents (amount × 100).
     */
    public CreatePaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) {
        try {
            long amountInCents = request.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("eur")
                    .setDescription("Campway booking #" + request.getBookingId())
                    .putMetadata("bookingId", String.valueOf(request.getBookingId()))
                    .addPaymentMethodType("card")
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            log.info("Stripe PaymentIntent created: id={}, bookingId={}", intent.getId(), request.getBookingId());

            return new CreatePaymentIntentResponse(intent.getClientSecret(), publishableKey, intent.getId());
        } catch (StripeException e) {
            log.error("Stripe PaymentIntent creation failed: {}", e.getMessage());
            throw new RuntimeException("Payment initialization failed: " + e.getMessage());
        }
    }
}
