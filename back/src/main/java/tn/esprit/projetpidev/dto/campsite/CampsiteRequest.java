// Module: Official Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.campsite;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tn.esprit.projetpidev.domain.enums.CampsiteType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CampsiteRequest {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String country;

    @NotBlank
    private String city;

    private String address;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @Min(1)
    private Integer capacity;

    @NotNull
    private CampsiteType type;

    @DecimalMin(value = "0.0", message = "Price per night cannot be negative")
    private BigDecimal pricePerNight;

    /** Comma-separated picture URLs */
    private String pictures;

    /** Comma-separated amenities */
    private String amenities;

    private String rules;

    /** Optional: date from which the campsite opens (null = no restriction) */
    @FutureOrPresent(message = "Start date must be today or a future date")
    private LocalDate startDate;

    /** Optional: date after which the campsite expires (null = no restriction) */
    private LocalDate endDate;

    /** Comma-separated natural features e.g. "FOREST,LAKE" */
    private String naturalFeatures;
}
