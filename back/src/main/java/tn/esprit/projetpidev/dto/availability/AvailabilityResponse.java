// Module: Official Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.availability;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AvailabilityResponse {

    private Long id;
    private Long campsiteId;
    private String campsiteName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfPlaces;
    private String weatherCondition;
    private boolean isBlocked;
    private LocalDateTime createdAt;
}
