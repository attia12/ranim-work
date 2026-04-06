package tn.esprit.projetpidev.dto.event;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EventRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Date is required")
    private String date;

    @Min(value = 1, message = "Max participants must be at least 1")
    private Integer maxParticipants;

    private String status = "UPCOMING";

    private String imageUrl;

    private String category;

    @PositiveOrZero(message = "Price must be zero or positive")
    private Double price;

    @NotNull(message = "Organizer ID is required")
    private Long event_organizer_id;
}