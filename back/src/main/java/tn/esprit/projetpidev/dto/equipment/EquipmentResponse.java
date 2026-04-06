package tn.esprit.projetpidev.dto.equipment;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EquipmentResponse {
    private Long id;
    private String name;
    private String description;
    private Float pricePerDay;
    private Float purchasePrice;
    private Boolean availableForRent;
    private Boolean availableForSale;
    private Integer stock;
    private List<String> photos;
    private String specifications;
    private String condition;
    private Float weight;
    private Long categoryId;
    private String categoryName;
    private Long ownerId;
    private String ownerFullName;
    private LocalDateTime createdAt;

    private Long warehouseId;
    private String warehouseName;
}

