// Module: Outdoor Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.outdoorbooking;

import lombok.Data;
import tn.esprit.projetpidev.domain.enums.OutdoorBookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OutdoorBookingResponse {

    private Long id;
    private Long outdoorCampsiteId;
    private String outdoorCampsiteName;
    private String outdoorCampsiteCountry;
    private String outdoorCampsiteCity;
    private Long camperId;
    private String camperFullName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfGuests;
    private OutdoorBookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
