package tn.esprit.projetpidev.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tn.esprit.projetpidev.domain.enums.OrderType;
import tn.esprit.projetpidev.dto.orderItem.OrderItemRequest;

import java.util.List;

@Data
public class OrderRequest {

    @NotNull(message = "Order type is required")
    private OrderType type;

    private String notes;

    private String couponCode;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;
}

