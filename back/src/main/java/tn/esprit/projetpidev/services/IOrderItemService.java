package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.orderItem.OrderItemRequest;
import tn.esprit.projetpidev.dto.orderItem.OrderItemResponse;

import java.util.List;

public interface IOrderItemService {
    OrderItemResponse addItemToOrder(Long orderId, OrderItemRequest request);
    OrderItemResponse getOrderItemById(Long itemId);
    List<OrderItemResponse> getItemsByOrder(Long orderId);
    List<OrderItemResponse> getItemsByEquipment(Long equipmentId);
    OrderItemResponse updateOrderItem(Long itemId, OrderItemRequest request);
    void removeItemFromOrder(Long itemId);
}

