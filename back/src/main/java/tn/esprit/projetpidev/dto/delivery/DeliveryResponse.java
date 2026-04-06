package tn.esprit.projetpidev.dto.delivery;

import lombok.Data;
import tn.esprit.projetpidev.domain.enums.DeliveryStatus;

import java.time.LocalDateTime;

@Data
public class DeliveryResponse {
    private Long id;
    private DeliveryStatus status;
    private String pickupAddress;
    private String deliveryAddress;
    private Float actualCost;
    private Float earningAmount;
    private LocalDateTime scheduledPickupTime;
    private LocalDateTime scheduledDeliveryTime;
    private LocalDateTime actualPickupTime;
    private LocalDateTime actualDeliveryTime;
    private String proofOfDelivery;
    private String deliveryNotes;
    private Long vehicleId;
    private String vehiclePlate;
    private Long orderId;
    private String orderNumber;
    private Long camperId;
    private String camperFullName;
    private LocalDateTime createdAt;
}

