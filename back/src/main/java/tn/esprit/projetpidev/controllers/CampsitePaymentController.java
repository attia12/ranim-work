// Module: Official Campsite & Booking | Layer: Controller
package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.campsitepayment.CampsitePaymentRequest;
import tn.esprit.projetpidev.dto.campsitepayment.CampsitePaymentResponse;
import tn.esprit.projetpidev.services.ICampsitePaymentService;

@RestController
@RequestMapping("/api/v1/campsite-payments")
@RequiredArgsConstructor
@Tag(name = "Campsite Payments", description = "Campsite booking payment processing")
public class CampsitePaymentController {

    private final ICampsitePaymentService paymentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Process payment for a campsite booking")
    public ResponseEntity<CampsitePaymentResponse> pay(@Valid @RequestBody CampsitePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.pay(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<CampsitePaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payment by booking ID")
    public ResponseEntity<CampsitePaymentResponse> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getByBooking(bookingId));
    }

    @PatchMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: refund a payment")
    public ResponseEntity<CampsitePaymentResponse> refund(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.refund(id));
    }
}
