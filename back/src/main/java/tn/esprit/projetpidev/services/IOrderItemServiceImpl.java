package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.dto.orderItem.OrderItemRequest;
import tn.esprit.projetpidev.dto.orderItem.OrderItemResponse;
import tn.esprit.projetpidev.domain.Equipment;
import tn.esprit.projetpidev.domain.Order;
import tn.esprit.projetpidev.domain.OrderItem;
import tn.esprit.projetpidev.domain.enums.OrderStatus;
import tn.esprit.projetpidev.domain.enums.TransactionType;
import tn.esprit.projetpidev.repositories.EquipmentRepository;
import tn.esprit.projetpidev.repositories.OrderItemRepository;
import tn.esprit.projetpidev.repositories.OrderRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class IOrderItemServiceImpl implements IOrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final EquipmentRepository equipmentRepository;

    @Override
    public OrderItemResponse addItemToOrder(Long orderId, OrderItemRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("Items can only be added to DRAFT orders");
        }

        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", request.getEquipmentId()));

        int days = request.getRentalDays() != null ? request.getRentalDays() : 1;
        int qty  = request.getQuantity() != null ? request.getQuantity() : 1;

        if (equipment.getStock() < qty) {
            throw new IllegalStateException(
                    "Insufficient stock for '" + equipment.getName() + "'. Available: " + equipment.getStock()
            );
        }
        equipment.setStock(equipment.getStock() - qty);
        equipmentRepository.save(equipment);

        Float unitPrice;
        Float totalPrice;

        if (request.getTransactionType() == TransactionType.RENT) {
            unitPrice  = equipment.getPricePerDay() != null ? equipment.getPricePerDay() : 0f;
            totalPrice = unitPrice * days * qty;
        } else {
            unitPrice  = equipment.getPurchasePrice() != null ? equipment.getPurchasePrice() : 0f;
            totalPrice = unitPrice * qty;
        }

        OrderItem item = OrderItem.builder()
                .order(order)
                .equipment(equipment)
                .quantity(qty)
                .transactionType(request.getTransactionType())
                .rentalDays(request.getRentalDays())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .build();

        OrderItem saved = orderItemRepository.save(item);

        // Recalculate order total
        float existingTotal = order.getItems() == null ? 0f :
                (float) order.getItems().stream()
                        .mapToDouble(i -> i.getTotalPrice() != null ? i.getTotalPrice() : 0f)
                        .sum();
        order.setTotalAmount(existingTotal + totalPrice);
        orderRepository.save(order);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItemResponse getOrderItemById(Long itemId) {
        return mapToResponse(orderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", itemId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemResponse> getItemsByOrder(Long orderId) {
        return orderItemRepository.findByOrderId(orderId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemResponse> getItemsByEquipment(Long equipmentId) {
        return orderItemRepository.findByEquipmentId(equipmentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public OrderItemResponse updateOrderItem(Long itemId, OrderItemRequest request) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", itemId));

        if (item.getOrder().getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("Items can only be modified in DRAFT orders");
        }

        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", request.getEquipmentId()));

        int days = request.getRentalDays() != null ? request.getRentalDays() : 1;
        int qty  = request.getQuantity() != null ? request.getQuantity() : 1;

        int delta = qty - item.getQuantity();
        if (equipment.getStock() < delta) {
            throw new IllegalStateException(
                    "Insufficient stock for '" + equipment.getName() + "'. Available: " + equipment.getStock()
            );
        }
        equipment.setStock(equipment.getStock() - delta);
        equipmentRepository.save(equipment);

        Float unitPrice;
        Float totalPrice;

        if (request.getTransactionType() == TransactionType.RENT) {
            unitPrice  = equipment.getPricePerDay() != null ? equipment.getPricePerDay() : 0f;
            totalPrice = unitPrice * days * qty;
        } else {
            unitPrice  = equipment.getPurchasePrice() != null ? equipment.getPurchasePrice() : 0f;
            totalPrice = unitPrice * qty;
        }

        item.setEquipment(equipment);
        item.setQuantity(qty);
        item.setTransactionType(request.getTransactionType());
        item.setRentalDays(request.getRentalDays());
        item.setUnitPrice(unitPrice);
        item.setTotalPrice(totalPrice);

        return mapToResponse(orderItemRepository.save(item));
    }

    @Override
    public void removeItemFromOrder(Long itemId) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", itemId));

        if (item.getOrder().getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("Items can only be removed from DRAFT orders");
        }

        Order order = item.getOrder();

        Equipment eq = item.getEquipment();
        eq.setStock(eq.getStock() + item.getQuantity());
        equipmentRepository.save(eq);

        Float removedAmount = item.getTotalPrice() != null ? item.getTotalPrice() : 0f;
        Float currentTotal  = order.getTotalAmount() != null ? order.getTotalAmount() : 0f;
        order.setTotalAmount(Math.max(0f, currentTotal - removedAmount));
        orderRepository.save(order);

        orderItemRepository.deleteById(itemId);
    }

    private OrderItemResponse mapToResponse(OrderItem item) {
        OrderItemResponse r = new OrderItemResponse();
        r.setId(item.getId());
        r.setEquipmentId(item.getEquipment().getId());
        r.setEquipmentName(item.getEquipment().getName());
        r.setQuantity(item.getQuantity());
        r.setTransactionType(item.getTransactionType());
        r.setRentalDays(item.getRentalDays());
        r.setUnitPrice(item.getUnitPrice());
        r.setTotalPrice(item.getTotalPrice());
        return r;
    }
}
