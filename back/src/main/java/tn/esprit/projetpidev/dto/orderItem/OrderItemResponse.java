package tn.esprit.projetpidev.dto.orderItem;

import lombok.Data;
import tn.esprit.projetpidev.domain.enums.TransactionType;

@Data
public class OrderItemResponse {
    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private Integer quantity;
    private TransactionType transactionType;
    private Integer rentalDays;
    private Float unitPrice;
    private Float totalPrice;
}

