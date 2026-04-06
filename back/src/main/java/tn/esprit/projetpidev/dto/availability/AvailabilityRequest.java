// Module: Official Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.availability;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AvailabilityRequest {

    @NotNull
    private Long campsiteId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Min(0)
    private Integer numberOfPlaces;

    private String weatherCondition;
    private boolean isBlocked;
}
