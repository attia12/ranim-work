// BookingRequest.java
package tn.esprit.projetpidev.dto.booking;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {
    @NotNull private Long eventId;
    @NotNull private Long userId;
}