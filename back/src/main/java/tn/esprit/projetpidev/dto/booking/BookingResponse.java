package tn.esprit.projetpidev.dto.booking;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private String eventDate;
    private String eventLocation;
    private Long userId;
    private String status;
    private LocalDateTime bookingDate;
}