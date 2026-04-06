package tn.esprit.projetpidev.dto.review;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private Long equipmentId;
    private String equipmentName;
    private Long authorId;
    private String authorFullName;
    private LocalDateTime createdAt;
}

