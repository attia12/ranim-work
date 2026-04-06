package tn.esprit.projetpidev.dto.deliveryrating;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeliveryRatingResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private Long deliveryId;
    private Long authorId;
    private String authorFullName;
    private LocalDateTime createdAt;
}

