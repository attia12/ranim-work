package tn.esprit.projetpidev.dto.category;

import lombok.Data;

@Data
public class CategoryResponse {
    private Long categoryId;
    private String name;
    private String description;
    private int totalPosts;
}