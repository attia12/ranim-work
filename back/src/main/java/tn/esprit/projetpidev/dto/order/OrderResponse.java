package tn.esprit.projetpidev.dto.order;

import lombok.Data;
import tn.esprit.projetpidev.domain.enums.OrderStatus;
import tn.esprit.projetpidev.domain.enums.OrderType;
import tn.esprit.projetpidev.dto.orderItem.OrderItemResponse;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private OrderType type;
    private OrderStatus status;
    private Float totalAmount;
    private Float shippingCost;
    private Float taxAmount;
    private Float discountAmount;
    private String couponCode;
    private String notes;
    private Long camperId;
    private String camperFullName;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime validatedAt;
}

