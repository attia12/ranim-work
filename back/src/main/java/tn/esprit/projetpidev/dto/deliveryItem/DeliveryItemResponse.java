package tn.esprit.projetpidev.dto.deliveryItem;

import lombok.Data;

import java.time.LocalDateTime;

import tn.esprit.projetpidev.domain.enums.TransactionType;

@Data
public class DeliveryItemResponse {
    private Long id;
    private Integer quantity;
    private TransactionType transactionType;
    private Long deliveryId;
    private LocalDateTime createdAt;
}

