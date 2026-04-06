package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.domain.enums.DiscountType;
import tn.esprit.projetpidev.domain.enums.OrderStatus;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.domain.enums.TransactionType;
import tn.esprit.projetpidev.dto.orderItem.OrderItemRequest;
import tn.esprit.projetpidev.dto.orderItem.OrderItemResponse;
import tn.esprit.projetpidev.dto.order.OrderRequest;
import tn.esprit.projetpidev.dto.order.OrderResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.CouponRepository;
import tn.esprit.projetpidev.repositories.EquipmentRepository;
import tn.esprit.projetpidev.repositories.OrderRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class IOrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final EquipmentRepository equipmentRepository;
    private final CouponRepository couponRepository;
    private final ICouponService couponService;

    @Override
    public OrderResponse createOrder(OrderRequest request, User loggedInUser) {
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .type(request.getType())
                .status(OrderStatus.DRAFT)
                .notes(request.getNotes())
                .camper(loggedInUser)
                .shippingCost(0f)
                .taxAmount(0f)
                .discountAmount(0f)
                .build();

        Float total = 0f;
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            Equipment equipment = equipmentRepository.findById(itemReq.getEquipmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Equipment", itemReq.getEquipmentId()));

            Integer qty = itemReq.getQuantity() != null ? itemReq.getQuantity() : 1;

            if (equipment.getStock() < qty) {
                throw new IllegalStateException(
                        "Insufficient stock for '" + equipment.getName() + "'. Available: " + equipment.getStock()
                );
            }
            equipment.setStock(equipment.getStock() - qty);
            equipmentRepository.save(equipment);

            Float unitPrice;

            if (itemReq.getTransactionType() == TransactionType.RENT) {
                Integer days = itemReq.getRentalDays() != null ? itemReq.getRentalDays() : 1;
                unitPrice = equipment.getPricePerDay() != null ? equipment.getPricePerDay() : 0f;
                Float itemTotal = unitPrice * days * qty;
                total = total + itemTotal;

                items.add(OrderItem.builder()
                        .quantity(qty)
                        .transactionType(TransactionType.RENT)
                        .rentalDays(days)
                        .unitPrice(unitPrice)
                        .totalPrice(itemTotal)
                        .order(order)
                        .equipment(equipment)
                        .build());
            } else {
                unitPrice = equipment.getPurchasePrice() != null ? equipment.getPurchasePrice() : 0f;
                Float itemTotal = unitPrice * qty;
                total = total + itemTotal;

                items.add(OrderItem.builder()
                        .quantity(qty)
                        .transactionType(TransactionType.BUY)
                        .unitPrice(unitPrice)
                        .totalPrice(itemTotal)
                        .order(order)
                        .equipment(equipment)
                        .build());
            }
        }

        order.setItems(items);
        order.setTotalAmount(total);

        if (request.getCouponCode() != null && !request.getCouponCode().isEmpty()) {
            Coupon coupon = couponService.validateCoupon(request.getCouponCode(), total);
            Float discount = computeDiscount(coupon, total);
            order.setDiscountAmount(discount);
            order.setTotalAmount(total - discount);
            order.setCouponCode(request.getCouponCode().toUpperCase());
            coupon.setCurrentUsageCount(coupon.getCurrentUsageCount() + 1);
            couponRepository.save(coupon);
        }

        return mapToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, User requester) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        boolean isOwner    = order.getCamper().getId().equals(requester.getId());
        boolean isAdmin    = requester.getRole() == Role.ADMIN;
        boolean isProvider = requester.getRole() == Role.EQUIPEMENTPROVIEDERS;
        if (!isOwner && !isAdmin && !isProvider) {
            throw new IllegalArgumentException("You are not authorized to view this order");
        }
        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByCamperIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::mapToResponse).toList();
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus, User loggedInUser) {
        if (loggedInUser.getRole() != Role.ADMIN && loggedInUser.getRole() != Role.EQUIPEMENTPROVIEDERS) {
            throw new IllegalArgumentException("Only admins or providers can update order status");
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        order.setStatus(newStatus);
        if (newStatus == OrderStatus.VALIDATED) {
            order.setValidatedAt(LocalDateTime.now());
        }
        return mapToResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse applyCoupon(Long orderId, String couponCode, User requester) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!order.getCamper().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("You can only apply coupons to your own orders");
        }

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("Coupons can only be applied to DRAFT orders");
        }

        Float rawTotal = order.getItems() == null ? 0f :
                (float) order.getItems().stream()
                        .mapToDouble(i -> i.getTotalPrice() != null ? i.getTotalPrice() : 0f)
                        .sum();

        Coupon coupon = couponService.validateCoupon(couponCode, rawTotal);
        Float discount = computeDiscount(coupon, rawTotal);

        order.setDiscountAmount(discount);
        order.setTotalAmount(rawTotal - discount);
        order.setCouponCode(couponCode.toUpperCase());

        coupon.setCurrentUsageCount(Integer.valueOf(coupon.getCurrentUsageCount() + 1));
        couponRepository.save(coupon);

        return mapToResponse(orderRepository.save(order));
    }

    @Override
    public void deleteOrder(Long id, User loggedInUser) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        if (order.getStatus() != OrderStatus.DRAFT && order.getStatus() != OrderStatus.CANCELLED) {
            throw new IllegalStateException("Only DRAFT or CANCELLED orders can be deleted");
        }

        boolean isOwner = order.getCamper().getId().equals(loggedInUser.getId());
        boolean isAdmin = loggedInUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("You are not authorized to delete this order");
        }

        order.getItems().forEach(item -> {
            Equipment eq = item.getEquipment();
            eq.setStock(eq.getStock() + item.getQuantity());
            equipmentRepository.save(eq);
        });

        orderRepository.deleteById(id);
    }

    private String generateOrderNumber() {
        String candidate;
        do {
            candidate = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (orderRepository.existsByOrderNumber(candidate));
        return candidate;
    }

    private Float computeDiscount(Coupon coupon, Float total) {
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            return total * (coupon.getDiscountValue().floatValue() / 100f);
        } else {
            return Math.min(coupon.getDiscountValue().floatValue(), total.floatValue());
        }
    }

    private OrderItemResponse mapItemToResponse(OrderItem item) {
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

    private OrderResponse mapToResponse(Order order) {
        OrderResponse r = new OrderResponse();
        r.setId(order.getId());
        r.setOrderNumber(order.getOrderNumber());
        r.setType(order.getType());
        r.setStatus(order.getStatus());
        r.setTotalAmount(order.getTotalAmount());
        r.setShippingCost(order.getShippingCost());
        r.setTaxAmount(order.getTaxAmount());
        r.setDiscountAmount(order.getDiscountAmount());
        r.setCouponCode(order.getCouponCode());
        r.setNotes(order.getNotes());
        r.setCamperId(order.getCamper().getId());
        r.setCamperFullName(order.getCamper().getFullname());
        r.setCreatedAt(order.getCreatedAt());
        r.setValidatedAt(order.getValidatedAt());
        r.setItems(order.getItems() != null
                ? order.getItems().stream().map(this::mapItemToResponse).toList()
                : List.of());
        return r;
    }
}

