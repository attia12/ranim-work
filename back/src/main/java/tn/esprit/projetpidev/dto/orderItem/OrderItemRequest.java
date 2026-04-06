package tn.esprit.projetpidev.dto.orderItem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tn.esprit.projetpidev.domain.enums.TransactionType;

@Data
public class OrderItemRequest {

    @NotNull(message = "Equipment ID is required")
    private Long equipmentId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    private Integer rentalDays;
}

