package tn.esprit.projetpidev.dto.equipment;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class EquipmentRequest {

    @NotBlank(message = "Equipment name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    @Positive(message = "Price per day must be positive")
    private Float pricePerDay;

    @Positive(message = "Purchase price must be positive")
    private Float purchasePrice;

    private boolean availableForRent;

    private boolean availableForSale;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private List<String> photos;

    private String specifications;

    private String condition;

    @Positive(message = "Weight must be positive")
    private Float weight;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @AssertTrue(message = "pricePerDay is required when availableForRent is true")
    public boolean isPricePerDayValid() {
        return !availableForRent || pricePerDay != null;
    }

    @AssertTrue(message = "purchasePrice is required when availableForSale is true")
    public boolean isPurchasePriceValid() {
        return !availableForSale || purchasePrice != null;
    }

    @AssertTrue(message = "Equipment must be available for at least rent or sale")
    public boolean isAvailabilityValid() {
        return availableForRent || availableForSale;
    }

    private Long warehouseId; // nullable
}

