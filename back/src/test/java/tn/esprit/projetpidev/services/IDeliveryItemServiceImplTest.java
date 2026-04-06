package tn.esprit.projetpidev.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.domain.enums.*;
import tn.esprit.projetpidev.domain.enums.TransactionType;
import tn.esprit.projetpidev.dto.deliveryItem.DeliveryItemRequest;
import tn.esprit.projetpidev.dto.deliveryItem.DeliveryItemResponse;
import tn.esprit.projetpidev.repositories.DeliveryItemRepository;
import tn.esprit.projetpidev.repositories.DeliveryRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IDeliveryItemServiceImplTest {

    @Mock
    private DeliveryItemRepository deliveryItemRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private IDeliveryItemServiceImpl deliveryItemService;

    private User camper;
    private User otherUser;
    private Delivery delivery;
    private DeliveryItemRequest request;
    private DeliveryItem savedItem;

    @BeforeEach
    void setUp() {
        camper = new User();
        camper.setId(1L);
        camper.setRole(Role.COMPERS);

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setRole(Role.COMPERS);

        delivery = new Delivery();
        delivery.setId(100L);
        delivery.setCamper(camper);
        delivery.setStatus(DeliveryStatus.PENDING_ASSIGNMENT);

        request = new DeliveryItemRequest();
        request.setDeliveryId(100L);
        request.setQuantity(2);
        request.setTransactionType(TransactionType.BUY);

        savedItem = DeliveryItem.builder()
                .id(10L)
                .delivery(delivery)
                .quantity(request.getQuantity())
                .transactionType(request.getTransactionType())
                .build();
    }

    @Test
    void testAddItemToDelivery_Success() {
        // Mocking
        when(deliveryRepository.findById(request.getDeliveryId())).thenReturn(Optional.of(delivery));
        when(deliveryItemRepository.save(any(DeliveryItem.class))).thenReturn(savedItem);

        // Action
        DeliveryItemResponse response = deliveryItemService.addItemToDelivery(request, camper);

        // Verification
        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(2, response.getQuantity());
        assertEquals(TransactionType.BUY, response.getTransactionType());
        assertEquals(100L, response.getDeliveryId());

        verify(deliveryRepository, times(1)).findById(request.getDeliveryId());
        verify(deliveryItemRepository, times(1)).save(any(DeliveryItem.class));
    }

    @Test
    void testAddItemToDelivery_ThrowsExceptionWhenNotOwner() {
        // Mocking
        when(deliveryRepository.findById(request.getDeliveryId())).thenReturn(Optional.of(delivery));

        // Action & Verification
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deliveryItemService.addItemToDelivery(request, otherUser);
        });

        assertEquals("Only the camper who created this delivery or an admin can add items", exception.getMessage());
        verify(deliveryItemRepository, never()).save(any(DeliveryItem.class));
    }

    @Test
    void testAddItemToDelivery_ThrowsExceptionWhenNotPendingStatus() {
        // Mocking
        delivery.setStatus(DeliveryStatus.IN_TRANSIT);
        when(deliveryRepository.findById(request.getDeliveryId())).thenReturn(Optional.of(delivery));

        // Action & Verification
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            deliveryItemService.addItemToDelivery(request, camper);
        });

        assertEquals("Items can only be added to PENDING_ASSIGNMENT deliveries", exception.getMessage());
        verify(deliveryItemRepository, never()).save(any(DeliveryItem.class));
    }
}

