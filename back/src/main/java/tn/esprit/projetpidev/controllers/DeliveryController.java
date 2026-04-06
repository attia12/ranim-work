package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.delivery.DeliveryRequest;
import tn.esprit.projetpidev.dto.delivery.DeliveryResponse;
import tn.esprit.projetpidev.dto.delivery.DeliveryStatusUpdateRequest;
import tn.esprit.projetpidev.domain.enums.DeliveryStatus;
import tn.esprit.projetpidev.services.IDeliveryService;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/delivery/deliveries")
@RequiredArgsConstructor
@Tag(name = " Deliveries", description = "Create and track deliveries")
public class DeliveryController {

    private final IDeliveryService deliveryService;
    private final UserRepository userRepo;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryResponse>> getAll() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('DELIVERYAGENT')")
    public ResponseEntity<List<DeliveryResponse>> getAvailable() {
        return ResponseEntity.ok(deliveryService.getAvailableDeliveries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getById(@PathVariable Long id, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(deliveryService.getDeliveryById(id, user));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DeliveryResponse>> getMy(Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(deliveryService.getDeliveriesByCamper(user.getId()));
    }

    @GetMapping("/agent")
    @PreAuthorize("hasRole('DELIVERYAGENT')")
    public ResponseEntity<List<DeliveryResponse>> getMyAgentDeliveries(Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(deliveryService.getDeliveriesByAgent(user.getId()));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryResponse>> getByStatus(@PathVariable DeliveryStatus status) {
        return ResponseEntity.ok(deliveryService.getDeliveriesByStatus(status));
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<DeliveryResponse> getByOrderId(@PathVariable Long orderId) {
        return deliveryService.getDeliveryByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasAnyRole('DELIVERYAGENT','ADMIN')")
    public ResponseEntity<List<DeliveryResponse>> getByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(deliveryService.getDeliveriesByVehicle(vehicleId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeliveryResponse> create(@Valid @RequestBody DeliveryRequest request,
                                                    Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(deliveryService.createDelivery(request, user));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryResponse> assignVehicle(@PathVariable Long id,
                                                           @RequestParam Long vehicleId,
                                                           Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(deliveryService.assignVehicle(id, vehicleId, user));
    }

    @PutMapping("/{id}/claim")
    @PreAuthorize("hasRole('DELIVERYAGENT')")
    public ResponseEntity<DeliveryResponse> claimDelivery(@PathVariable Long id,
                                                           Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(deliveryService.claimDelivery(id, user));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DELIVERYAGENT','ADMIN')")
    public ResponseEntity<DeliveryResponse> updateStatus(@PathVariable Long id,
                                                          @Valid @RequestBody DeliveryStatusUpdateRequest request,
                                                          Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(deliveryService.updateStatus(id, request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        deliveryService.deleteDelivery(id);
        return ResponseEntity.ok("Delivery deleted successfully!");
    }
}

