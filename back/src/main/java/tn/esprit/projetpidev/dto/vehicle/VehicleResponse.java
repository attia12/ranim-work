package tn.esprit.projetpidev.dto.vehicle;

import lombok.Data;
import tn.esprit.projetpidev.domain.enums.AvailabilityStatus;
import tn.esprit.projetpidev.domain.enums.VehicleType;

import java.time.LocalDateTime;

@Data
public class VehicleResponse {
    private Long id;
    private VehicleType type;
    private String brand;
    private String model;
    private Integer year;
    private String color;
    private String plate;
    private Float serviceRadius;
    private Integer maxConcurrentDeliveries;
    private Integer currentDeliveriesCount;
    private AvailabilityStatus availabilityStatus;
    private Boolean isVerified;
    private LocalDateTime insuranceExpiryDate;
    private Long ownerId;
    private String ownerFullName;
}

