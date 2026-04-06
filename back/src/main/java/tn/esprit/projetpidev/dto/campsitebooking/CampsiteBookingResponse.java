// Module: Official Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.campsitebooking;

import lombok.Data;
import tn.esprit.projetpidev.domain.enums.CampsiteBookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CampsiteBookingResponse {

    private Long id;
    private Long campsiteId;
    private String campsiteName;
    private String campsiteCountry;
    private String campsiteCity;
    private Long camperId;
    private String camperFullName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfGuests;
    private BigDecimal totalPrice;
    private CampsiteBookingStatus status;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
