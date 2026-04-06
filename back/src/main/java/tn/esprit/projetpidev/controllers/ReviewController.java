package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.dto.review.ReviewRequest;
import tn.esprit.projetpidev.dto.review.ReviewResponse;
import tn.esprit.projetpidev.services.IReviewService;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace/reviews")
@RequiredArgsConstructor
@Tag(name = " Reviews", description = "Write and read equipment reviews")
public class ReviewController {

    private final IReviewService reviewService;
    private final UserRepository userRepo;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReviewResponse>> getAll() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/equipment/{id}")
    public ResponseEntity<List<ReviewResponse>> getByEquipment(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewsByEquipment(id));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<ReviewResponse>> getByUser(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewRequest request,
            Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(reviewService.createReview(request, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ReviewRequest request,
                                                  Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(reviewService.updateReview(id, request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> delete(@PathVariable Long id, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        reviewService.deleteReview(id, user);
        return ResponseEntity.ok("Review deleted successfully!");
    }
}
