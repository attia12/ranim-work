package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.Delivery;
import tn.esprit.projetpidev.domain.enums.DeliveryStatus;
import tn.esprit.projetpidev.repositories.DeliveryRepository;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.dto.payment.PaymentRequest;
import tn.esprit.projetpidev.dto.payment.PaymentResponse;
import tn.esprit.projetpidev.domain.Order;
import tn.esprit.projetpidev.domain.Payment;
import tn.esprit.projetpidev.domain.enums.OrderStatus;
import tn.esprit.projetpidev.domain.enums.PaymentStatus;
import tn.esprit.projetpidev.repositories.OrderRepository;
import tn.esprit.projetpidev.repositories.PaymentRepository;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class IPaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;

    @Override
    public PaymentResponse createPayment(PaymentRequest request, User requester) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));

        if (!order.getCamper().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("You can only pay for your own orders");
        }

        if (order.getStatus() != OrderStatus.DRAFT
                && order.getStatus() != OrderStatus.PENDING_PAYMENT
                && order.getStatus() != OrderStatus.ACTIVE
                && order.getStatus() != OrderStatus.VALIDATED) {
            throw new IllegalStateException("Order is not in a payable state. Current status: " + order.getStatus());
        }

        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new IllegalStateException("A payment already exists for this order");
        }

        Payment payment = Payment.builder()
                .amount(order.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.COMPLETED)       // immediately mark as COMPLETED
                .transactionId(request.getTransactionId())
                .completedAt(LocalDateTime.now())             // set completed timestamp
                .order(order)
                .build();

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // AUTO-CREATE delivery so agents can see it immediately
        if (!deliveryRepository.existsByOrderId(order.getId())) {
            Delivery delivery = Delivery.builder()
                    .status(DeliveryStatus.PENDING_ASSIGNMENT)
                    .pickupAddress("Provider Warehouse - Tunis")
                    .deliveryAddress(order.getCamper().getFirstName() + " " +
                                     order.getCamper().getLastName() + " - Address on file")
                    .scheduledPickupTime(java.time.LocalDateTime.now().plusDays(1))
                    .scheduledDeliveryTime(java.time.LocalDateTime.now().plusDays(2))
                    .deliveryNotes("Order " + order.getOrderNumber() + " — auto-created on payment")
                    .camper(order.getCamper())
                    .order(order)
                    .build();
            deliveryRepository.save(delivery);
        }

        return mapToResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse completePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Payment is already completed");
        }

        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(LocalDateTime.now());
        payment.getOrder().setStatus(OrderStatus.PAID);
        orderRepository.save(payment.getOrder());

        return mapToResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.getOrder().setStatus(OrderStatus.REFUNDED);
        orderRepository.save(payment.getOrder());

        return mapToResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        return mapToResponse(paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PaymentResponse mapToResponse(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.setId(p.getId());
        r.setOrderId(p.getOrder().getId());
        r.setOrderNumber(p.getOrder().getOrderNumber());
        r.setAmount(p.getAmount());
        r.setPaymentMethod(p.getPaymentMethod());
        r.setPaymentStatus(p.getPaymentStatus());
        r.setTransactionId(p.getTransactionId());
        r.setCreatedAt(p.getCreatedAt());
        r.setCompletedAt(p.getCompletedAt());
        return r;
    }
}
