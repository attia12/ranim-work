package tn.esprit.projetpidev.dto.deliveryItem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tn.esprit.projetpidev.domain.enums.TransactionType;

@Data
public class DeliveryItemRequest {

    private Long deliveryId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;
}

