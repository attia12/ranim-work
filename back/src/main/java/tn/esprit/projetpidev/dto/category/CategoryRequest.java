package tn.esprit.projetpidev.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    // Category name - required, between 2 and 50 characters
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    // Category description - required, between 5 and 200 characters
    @NotBlank(message = "Description is required")
    @Size(min = 5, max = 200, message = "Description must be between 5 and 200 characters")
    private String description;
}