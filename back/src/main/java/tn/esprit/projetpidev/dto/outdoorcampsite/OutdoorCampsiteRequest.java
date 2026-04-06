// Module: Outdoor Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.outdoorcampsite;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import tn.esprit.projetpidev.domain.enums.AccessDifficulty;

@Data
public class OutdoorCampsiteRequest {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String country;

    @NotBlank
    private String city;

    private Double latitude;
    private Double longitude;

    /** Comma-separated picture URLs */
    private String pictures;

    /** Comma-separated natural features */
    private String naturalFeatures;

    private AccessDifficulty accessDifficulty;
}
