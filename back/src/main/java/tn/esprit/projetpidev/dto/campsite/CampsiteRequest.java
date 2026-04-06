// Module: Official Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.campsite;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tn.esprit.projetpidev.domain.enums.CampsiteType;

import java.math.BigDecimal;

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
    private Double latitude;
    private Double longitude;

    @Min(1)
    private Integer capacity;

    @NotNull
    private CampsiteType type;

    @DecimalMin("0.0")
    private BigDecimal pricePerNight;

    /** Comma-separated picture URLs */
    private String pictures;

    /** Comma-separated amenities */
    private String amenities;

    private String rules;
}
