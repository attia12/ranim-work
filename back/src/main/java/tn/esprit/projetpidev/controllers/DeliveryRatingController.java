package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.deliveryrating.DeliveryRatingRequest;
import tn.esprit.projetpidev.dto.deliveryrating.DeliveryRatingResponse;
import tn.esprit.projetpidev.services.IDeliveryRatingService;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.repositories.UserRepository;

@RestController
@RequestMapping("/api/delivery/ratings")
@RequiredArgsConstructor
@Tag(name = " Delivery Ratings", description = "Rate completed deliveries")
public class DeliveryRatingController {

    private final IDeliveryRatingService ratingService;
    private final UserRepository userRepo;

    @GetMapping("/delivery/{id}")
    public ResponseEntity<DeliveryRatingResponse> getByDelivery(@PathVariable Long id) {
        return ResponseEntity.ok(ratingService.getRatingByDelivery(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('COMPERS')")
    public ResponseEntity<DeliveryRatingResponse> create(@Valid @RequestBody DeliveryRatingRequest request,
                                                          Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(ratingService.createRating(request, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPERS','ADMIN')")
    public ResponseEntity<DeliveryRatingResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody DeliveryRatingRequest request,
                                                          Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(ratingService.updateRating(id, request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPERS','ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        ratingService.deleteRating(id, user);
        return ResponseEntity.ok("Rating deleted successfully!");
    }
}

