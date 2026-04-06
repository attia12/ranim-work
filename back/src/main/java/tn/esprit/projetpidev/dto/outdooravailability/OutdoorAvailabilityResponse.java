// Module: Outdoor Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.outdooravailability;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OutdoorAvailabilityResponse {

    private Long id;
    private Long outdoorCampsiteId;
    private String outdoorCampsiteName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isAvailable;
    private boolean isFullyBooked;
    private String note;
}
