// Module: Official Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.campsitebooking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CampsiteBookingRequest {

    @NotNull
    private Long campsiteId;

    @NotNull
    private LocalDate checkInDate;

    @NotNull
    private LocalDate checkOutDate;

    @Min(1)
    private Integer numberOfGuests;
}
