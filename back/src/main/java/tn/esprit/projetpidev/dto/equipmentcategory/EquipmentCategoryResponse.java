package tn.esprit.projetpidev.dto.equipmentcategory;

import lombok.Data;

@Data
public class EquipmentCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private Long parentId;
    private String parentName;
    private int childCount;
}

