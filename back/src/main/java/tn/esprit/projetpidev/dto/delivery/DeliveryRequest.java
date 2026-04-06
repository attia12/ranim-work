package tn.esprit.projetpidev.dto.delivery;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeliveryRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotBlank(message = "Pickup address is required")
    @Size(min = 5, max = 255, message = "Pickup address must be between 5 and 255 characters")
    private String pickupAddress;

    @NotBlank(message = "Delivery address is required")
    @Size(min = 5, max = 255, message = "Delivery address must be between 5 and 255 characters")
    private String deliveryAddress;

    @NotNull(message = "Scheduled pickup time is required")
    @Future(message = "Scheduled pickup time must be in the future")
    private LocalDateTime scheduledPickupTime;

    @NotNull(message = "Scheduled delivery time is required")
    @Future(message = "Scheduled delivery time must be in the future")
    private LocalDateTime scheduledDeliveryTime;

    private String deliveryNotes;

    @Positive(message = "Actual cost must be positive")
    private Float actualCost;
}

