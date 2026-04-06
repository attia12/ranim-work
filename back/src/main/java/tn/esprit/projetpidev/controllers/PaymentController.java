package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.dto.payment.PaymentRequest;
import tn.esprit.projetpidev.dto.payment.PaymentResponse;
import tn.esprit.projetpidev.repositories.UserRepository;
import tn.esprit.projetpidev.services.IPaymentService;

@RestController
@RequestMapping("/api/marketplace/payments")
@RequiredArgsConstructor
@Tag(name = " Payments", description = "Process payments for orders")
public class PaymentController {

    private final IPaymentService paymentService;
    private final UserRepository userRepo;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<PaymentResponse>> getAll() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest request,
                                                   Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(paymentService.createPayment(request, user));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.completePayment(id));
    }

    @PutMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> refund(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.refundPayment(id));
    }
}
