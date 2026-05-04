package tn.esprit.projetpidev.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.dto.recommendation.RecommendedCampsiteDTO;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.UserRepository;
import tn.esprit.projetpidev.services.RecommendationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/campsites")
@RequiredArgsConstructor
public class CampsiteRecommendationController {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    @GetMapping("/recommended")
    public ResponseEntity<List<RecommendedCampsiteDTO>> getRecommendations(
            Authentication authentication) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        List<RecommendedCampsiteDTO> recommendations =
                recommendationService.getRecommendations(user);

        return ResponseEntity.ok(recommendations);
    }
}
