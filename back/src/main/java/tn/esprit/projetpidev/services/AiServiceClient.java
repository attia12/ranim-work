package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tn.esprit.projetpidev.dto.recommendation.AiRequestDTO;
import tn.esprit.projetpidev.dto.recommendation.RecommendedCampsiteDTO;
import tn.esprit.projetpidev.exception.RecommendationUnavailableException;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceClient {

    private final WebClient aiWebClient;

    public List<RecommendedCampsiteDTO> fetchRecommendations(AiRequestDTO request) {
        try {
            List<RecommendedCampsiteDTO> result = aiWebClient.post()
                    .uri("/api/v1/campsites/recommended")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            response -> response.bodyToMono(String.class).map(body -> {
                                log.error("AI service returned {}: {}", response.statusCode(), body);
                                throw new RecommendationUnavailableException(
                                        "AI service returned " + response.statusCode() + ": " + body);
                            })
                    )
                    .bodyToFlux(RecommendedCampsiteDTO.class)
                    .collectList()
                    .timeout(Duration.ofSeconds(8))
                    .block();

            return result != null ? result : List.of();

        } catch (RecommendationUnavailableException e) {
            throw e;
        } catch (WebClientResponseException e) {
            log.error("AI service HTTP error: {}", e.getStatusCode(), e);
            throw new RecommendationUnavailableException(
                    "AI service error: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("AI service call failed", e);
            throw new RecommendationUnavailableException(
                    "AI service unavailable: " + e.getClass().getSimpleName());
        }
    }
}
