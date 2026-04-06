package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.deliveryItem.DeliveryItemRequest;
import tn.esprit.projetpidev.dto.deliveryItem.DeliveryItemResponse;
import tn.esprit.projetpidev.services.IDeliveryItemService;
import org.springframework.security.core.Authentication;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/delivery/delivery-items")
@RequiredArgsConstructor
@Tag(name = " Delivery Items", description = "Manage items inside a delivery")
public class DeliveryItemController {

    private final IDeliveryItemService deliveryItemService;
    private final UserRepository userRepo;

    @GetMapping("/{itemId}")
    public ResponseEntity<DeliveryItemResponse> getById(@PathVariable Long itemId) {
        return ResponseEntity.ok(deliveryItemService.getDeliveryItemById(itemId));
    }

    @GetMapping("/delivery/{deliveryId}")
    public ResponseEntity<List<DeliveryItemResponse>> getByDelivery(@PathVariable Long deliveryId) {
        return ResponseEntity.ok(deliveryItemService.getItemsByDelivery(deliveryId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPERS','ADMIN')")
    public ResponseEntity<DeliveryItemResponse> add(@Valid @RequestBody DeliveryItemRequest request,
                                                     Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(deliveryItemService.addItemToDelivery(request, user));
    }

    @PutMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('COMPERS','ADMIN')")
    public ResponseEntity<DeliveryItemResponse> update(@PathVariable Long itemId,
                                                        @Valid @RequestBody DeliveryItemRequest request,
                                                        Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(deliveryItemService.updateDeliveryItem(itemId, request, user));
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('COMPERS','ADMIN')")
    public ResponseEntity<String> remove(@PathVariable Long itemId, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        deliveryItemService.removeDeliveryItem(itemId, user);
        return ResponseEntity.ok("Delivery item removed successfully!");
    }
}

