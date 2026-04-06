package tn.esprit.projetpidev.dto.like;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LikeRequest {

    // ID of the user who likes the post
    @NotNull(message = "User ID is required")
    private Long userId;

    // ID of the post being liked
    @NotNull(message = "Post ID is required")
    private Long postId;
}