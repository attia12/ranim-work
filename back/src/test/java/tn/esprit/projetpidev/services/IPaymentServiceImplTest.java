package tn.esprit.projetpidev.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.domain.enums.*;
import tn.esprit.projetpidev.dto.payment.PaymentRequest;
import tn.esprit.projetpidev.dto.payment.PaymentResponse;
import tn.esprit.projetpidev.repositories.DeliveryRepository;
import tn.esprit.projetpidev.repositories.OrderRepository;
import tn.esprit.projetpidev.repositories.PaymentRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IPaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private DeliveryRepository deliveryRepository;

    @InjectMocks
    private IPaymentServiceImpl paymentService;

    private User camper;
    private Order draftOrder;
    private PaymentRequest request;

    @BeforeEach
    void setUp() {
        camper = new User();
        camper.setId(1L);

        draftOrder = Order.builder()
                .id(100L)
                .camper(camper)
                .orderNumber("ORD-123")
                .status(OrderStatus.DRAFT) // is eligible for payment
                .totalAmount(150f)
                .build();

        request = new PaymentRequest();
        request.setOrderId(100L);
        request.setPaymentMethod(PaymentMethod.BANK_CARD);
    }

    @Test
    void testCreatePayment_Success() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(draftOrder));
        when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(10L);
            return p;
        });

        PaymentResponse response = paymentService.createPayment(request, camper);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(PaymentStatus.COMPLETED, response.getPaymentStatus());
        assertEquals(150f, response.getAmount());
        
        // Ensure Delivery auto-creation was triggered
        verify(deliveryRepository, times(1)).save(any(Delivery.class));
        verify(orderRepository, times(1)).save(draftOrder);
    }

    @Test
    void testCreatePayment_ThrowsWhenNotOwner() {
        User otherUser = new User();
        otherUser.setId(2L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(draftOrder));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.createPayment(request, otherUser);
        });

        assertTrue(exception.getMessage().contains("can only pay for your own orders"));
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}

