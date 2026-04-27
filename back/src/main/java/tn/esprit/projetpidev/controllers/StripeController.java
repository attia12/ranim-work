package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.stripe.CreatePaymentIntentRequest;
import tn.esprit.projetpidev.dto.stripe.CreatePaymentIntentResponse;
import tn.esprit.projetpidev.services.StripeService;

@RestController
@RequestMapping("/api/v1/stripe")
@RequiredArgsConstructor
@Tag(name = "Stripe", description = "Stripe payment integration")
public class StripeController {

    private final StripeService stripeService;

    @PostMapping("/payment-intent")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a Stripe PaymentIntent for a campsite booking")
    public ResponseEntity<CreatePaymentIntentResponse> createPaymentIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request) {
        return ResponseEntity.ok(stripeService.createPaymentIntent(request));
    }
}
