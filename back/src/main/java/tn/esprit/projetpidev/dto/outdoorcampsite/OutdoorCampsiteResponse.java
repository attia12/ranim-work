// Module: Outdoor Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.outdoorcampsite;

import lombok.Data;
import tn.esprit.projetpidev.domain.enums.AccessDifficulty;
import tn.esprit.projetpidev.domain.enums.OutdoorCampsiteStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OutdoorCampsiteResponse {

    private Long id;
    private String name;
    private String description;
    private String country;
    private String city;
    private Double latitude;
    private Double longitude;
    private List<String> pictures;
    private List<String> naturalFeatures;
    private AccessDifficulty accessDifficulty;
    private Long proposedById;
    private String proposedByName;
    private OutdoorCampsiteStatus status;
    private String adminNote;
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
