package tn.esprit.projetpidev.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.domain.enums.*;
import tn.esprit.projetpidev.dto.delivery.DeliveryRequest;
import tn.esprit.projetpidev.dto.delivery.DeliveryResponse;
import tn.esprit.projetpidev.repositories.AgentProfileRepository;
import tn.esprit.projetpidev.repositories.DeliveryRepository;
import tn.esprit.projetpidev.repositories.OrderRepository;
import tn.esprit.projetpidev.repositories.VehicleRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IDeliveryServiceImplTest {

    @Mock private DeliveryRepository deliveryRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private AgentProfileRepository agentProfileRepo;

    @InjectMocks
    private IDeliveryServiceImpl deliveryService;

    private User camper;
    private Order paidOrder;
    private DeliveryRequest request;

    @BeforeEach
    void setUp() {
        camper = new User();
        camper.setId(1L);

        paidOrder = Order.builder()
                .id(100L)
                .camper(camper)
                .status(OrderStatus.PAID) // Must be PAID
                .build();

        request = new DeliveryRequest();
        request.setOrderId(100L);
        request.setPickupAddress("Warehouse A");
        request.setDeliveryAddress("Home B");
        request.setScheduledPickupTime(LocalDateTime.now().plusDays(1));
        request.setScheduledDeliveryTime(LocalDateTime.now().plusDays(2));
    }

    @Test
    void testCreateDelivery_Success() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(paidOrder));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(i -> {
            Delivery d = i.getArgument(0);
            d.setId(10L);
            return d;
        });

        DeliveryResponse response = deliveryService.createDelivery(request, camper);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(DeliveryStatus.PENDING_ASSIGNMENT, response.getStatus()); // Statut initial par défaut
        verify(deliveryRepository, times(1)).save(any(Delivery.class));
    }
}

