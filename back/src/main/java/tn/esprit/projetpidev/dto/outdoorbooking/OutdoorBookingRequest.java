// Module: Outdoor Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.outdoorbooking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OutdoorBookingRequest {

    @NotNull
    private Long outdoorCampsiteId;

    @NotNull
    private LocalDate checkInDate;

    @NotNull
    private LocalDate checkOutDate;

    @Min(1)
    private Integer numberOfGuests;
}
