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
import tn.esprit.projetpidev.dto.orderItem.OrderItemResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.EquipmentRepository;
import tn.esprit.projetpidev.repositories.OrderItemRepository;
import tn.esprit.projetpidev.repositories.OrderRepository;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IOrderItemServiceImplTest {

    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private EquipmentRepository equipmentRepository;

    @InjectMocks
    private IOrderItemServiceImpl orderItemService;

    private Order draftOrder;
    private Equipment equipment;
    private OrderItemRequest request;
    private OrderItem savedItem;

    @BeforeEach
    void setUp() {
        draftOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.DRAFT)
                .totalAmount(0f)
                .items(new ArrayList<>())
                .build();

        equipment = Equipment.builder()
                .id(10L)
                .name("Camping Tent")
                .stock(5)
                .pricePerDay(10f)
                .purchasePrice(100f)
                .build();

        request = new OrderItemRequest();
        request.setEquipmentId(10L);
        request.setQuantity(2);
        request.setTransactionType(TransactionType.RENT);
        request.setRentalDays(3);

        savedItem = OrderItem.builder()
                .id(100L)
                .order(draftOrder)
                .equipment(equipment)
                .quantity(2)
                .transactionType(TransactionType.RENT)
                .rentalDays(3)
                .unitPrice(10f)
                .totalPrice(60f)
                .build();
    }

    @Test
    void testAddItemToOrder_SuccessRent() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(draftOrder));
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(equipment));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(savedItem);

        OrderItemResponse response = orderItemService.addItemToOrder(1L, request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(60f, response.getTotalPrice());
        assertEquals(3, equipment.getStock()); // stock is decrement by 2

        verify(equipmentRepository, times(1)).save(equipment);
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        verify(orderRepository, times(1)).save(draftOrder);
    }

    @Test
    void testAddItemToOrder_ThrowsWhenNotDraft() {
        draftOrder.setStatus(OrderStatus.VALIDATED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(draftOrder));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            orderItemService.addItemToOrder(1L, request);
        });

        assertTrue(exception.getMessage().contains("Items can only be added to DRAFT orders"));
        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }

    @Test
    void testAddItemToOrder_ThrowsWhenInsufficientStock() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(draftOrder));
        
        equipment.setStock(1);
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(equipment));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            orderItemService.addItemToOrder(1L, request); // requesting 2
        });

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }
}

