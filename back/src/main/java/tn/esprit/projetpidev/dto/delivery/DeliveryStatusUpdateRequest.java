package tn.esprit.projetpidev.dto.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tn.esprit.projetpidev.domain.enums.DeliveryStatus;

import java.time.LocalDateTime;

@Data
public class DeliveryStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private DeliveryStatus status;

    private LocalDateTime actualPickupTime;

    private LocalDateTime actualDeliveryTime;

    private String proofOfDelivery;
}

