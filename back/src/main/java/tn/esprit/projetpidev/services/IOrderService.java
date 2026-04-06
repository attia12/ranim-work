package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.dto.order.OrderRequest;
import tn.esprit.projetpidev.dto.order.OrderResponse;
import tn.esprit.projetpidev.domain.enums.OrderStatus;

import java.util.List;

public interface IOrderService {
    OrderResponse createOrder(OrderRequest request, User loggedInUser);
    OrderResponse getOrderById(Long id, User requester);
    List<OrderResponse> getAllOrders();
    List<OrderResponse> getOrdersByUser(Long userId);
    List<OrderResponse> getOrdersByStatus(OrderStatus status);
    OrderResponse updateOrderStatus(Long id, OrderStatus newStatus, User loggedInUser);
    OrderResponse applyCoupon(Long orderId, String couponCode, User requester);
    void deleteOrder(Long id, User loggedInUser);
}

