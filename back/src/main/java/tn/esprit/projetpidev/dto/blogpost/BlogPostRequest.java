package tn.esprit.projetpidev.dto.blogpost;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogPostRequest {

    // Post title - required, between 3 and 100 characters
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    // Post content - required, between 10 and 5000 characters
    @NotBlank(message = "Content is required")
    @Size(min = 10, max = 5000, message = "Content must be between 10 and 5000 characters")
    private String content;

    // Category ID - required
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    @NotNull(message = "User ID is required")
    private Long userId;


}