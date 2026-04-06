package tn.esprit.projetpidev.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {

    // Comment content - required, between 2 and 500 characters
    @NotBlank(message = "Content is required")
    @Size(min = 2, max = 500, message = "Content must be between 2 and 500 characters")
    private String content;

    // ID of the post to comment on - required
    @NotNull(message = "Post ID is required")
    private Long postId;

    // ID of the user who is commenting - required
    @NotNull(message = "User ID is required")
    private Long userId;
}