// Module: Outdoor Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.outdooravailability;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OutdoorAvailabilityRequest {

    @NotNull
    private Long outdoorCampsiteId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private boolean isAvailable = true;
    private String note;
}
