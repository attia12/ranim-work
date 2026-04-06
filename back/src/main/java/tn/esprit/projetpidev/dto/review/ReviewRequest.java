package tn.esprit.projetpidev.dto.review;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotNull(message = "Equipment ID is required")
    private Long equipmentId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    @NotBlank(message = "Comment is required")
    @Size(min = 5, max = 1000, message = "Comment must be between 5 and 1000 characters")
    private String comment;
}

