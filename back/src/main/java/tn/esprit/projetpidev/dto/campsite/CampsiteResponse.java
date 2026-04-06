// Module: Official Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.campsite;

import lombok.Data;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;
import tn.esprit.projetpidev.domain.enums.CampsiteType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CampsiteResponse {

    private Long id;
    private String name;
    private String description;
    private String country;
    private String city;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer capacity;
    private CampsiteType type;
    private BigDecimal pricePerNight;
    private List<String> pictures;
    private List<String> amenities;
    private String rules;
    private CampsiteStatus status;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
