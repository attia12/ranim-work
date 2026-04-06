package tn.esprit.projetpidev.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.domain.enums.*;
import tn.esprit.projetpidev.dto.orderItem.OrderItemRequest;
import tn.esprit.projetpidev.dto.order.OrderRequest;
import tn.esprit.projetpidev.dto.order.OrderResponse;
import tn.esprit.projetpidev.repositories.CouponRepository;
import tn.esprit.projetpidev.repositories.EquipmentRepository;
import tn.esprit.projetpidev.repositories.OrderRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IOrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private EquipmentRepository equipmentRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private ICouponService couponService;

    @InjectMocks
    private IOrderServiceImpl orderService;

    private User camper;
    private OrderRequest request;
    private Equipment expectedEquipment;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        camper = new User();
        camper.setId(1L);
        camper.setRole(Role.COMPERS);

        expectedEquipment = Equipment.builder()
                .id(10L)
                .name("Tent")
                .stock(10)
                .pricePerDay(15f)
                .build();

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setEquipmentId(10L);
        itemRequest.setQuantity(2);
        itemRequest.setTransactionType(TransactionType.RENT);
        itemRequest.setRentalDays(3);

        request = new OrderRequest();
        request.setType(OrderType.ORDER);
        request.setNotes("Leave at door");
        request.setItems(List.of(itemRequest));

        savedOrder = Order.builder()
                .id(100L)
                .camper(camper)
                .status(OrderStatus.DRAFT)
                .totalAmount(90f) // 15 * 2 * 3
                .type(OrderType.ORDER)
                .build();
    }

    @Test
    void testCreateOrder_Success() {
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(expectedEquipment));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(request, camper);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(OrderStatus.DRAFT, response.getStatus());
        assertEquals(90f, response.getTotalAmount());
        
        // stock decrémenté
        assertEquals(8, expectedEquipment.getStock());

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCreateOrder_ThrowsWhenInsufficientStock() {
        expectedEquipment.setStock(1);
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(expectedEquipment));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            orderService.createOrder(request, camper);
        });

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(orderRepository, never()).save(any(Order.class));
    }
}

