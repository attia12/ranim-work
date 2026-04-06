package tn.esprit.projetpidev.dto.vehicle;

import jakarta.validation.constraints.*;
import lombok.Data;
import tn.esprit.projetpidev.domain.enums.VehicleType;

import java.time.LocalDateTime;

@Data
public class VehicleRequest {

    @NotNull(message = "Vehicle type is required")
    private VehicleType type;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be valid")
    private Integer year;

    private String color;

    @NotBlank(message = "Plate number is required")
    @Pattern(regexp = "^[A-Z0-9 \\-]{2,20}$", message = "Invalid plate format (uppercase letters, digits, spaces, hyphens only)")
    private String plate;

    @Positive(message = "Service radius must be positive")
    private Float serviceRadius;

    @NotNull(message = "Max weight is required")
    @Positive(message = "Max weight must be positive")
    private Float maxWeight;

    @NotNull(message = "Max concurrent deliveries is required")
    @Min(value = 1, message = "Must allow at least 1 concurrent delivery")
    @Max(value = 20, message = "Max concurrent deliveries cannot exceed 20")
    private Integer maxConcurrentDeliveries;

    @Future(message = "Insurance expiry date must be in the future")
    private LocalDateTime insuranceExpiryDate;
}

