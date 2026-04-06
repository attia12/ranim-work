package tn.esprit.projetpidev.dto.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventResponse {

    private Long eventId;
    private String title;
    private String location;
    private String date;
    private Integer maxParticipants;
    private String status;
    private String imageUrl;
    private String category;
    private Double price;

    // Organizer info (dénormalisé pour éviter la récursion)
    private Long eventOrganizerId;
    private String organizerFullName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}