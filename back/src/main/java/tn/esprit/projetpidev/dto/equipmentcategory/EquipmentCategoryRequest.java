package tn.esprit.projetpidev.dto.equipmentcategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EquipmentCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    private String description;

    private String icon;

    private Long parentId;
}

