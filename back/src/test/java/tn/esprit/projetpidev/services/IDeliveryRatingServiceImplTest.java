package tn.esprit.projetpidev.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.domain.enums.*;
import tn.esprit.projetpidev.dto.deliveryrating.DeliveryRatingRequest;
import tn.esprit.projetpidev.dto.deliveryrating.DeliveryRatingResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.DeliveryRatingRepository;
import tn.esprit.projetpidev.repositories.DeliveryRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IDeliveryRatingServiceImplTest {

    @Mock private DeliveryRatingRepository ratingRepository;
    @Mock private DeliveryRepository deliveryRepository;

    @InjectMocks
    private IDeliveryRatingServiceImpl deliveryRatingService;

    private User camper;
    private Delivery delivery;
    private DeliveryRatingRequest request;

    @BeforeEach
    void setUp() {
        camper = new User();
        camper.setId(1L);

        delivery = new Delivery();
        delivery.setId(100L);
        delivery.setCamper(camper);
        delivery.setStatus(DeliveryStatus.DELIVERED);

        request = new DeliveryRatingRequest();
        request.setDeliveryId(100L);
        request.setRating(5);
        request.setComment("Great delivery!");
    }

    @Test
    void testCreateRating_Success() {
        when(deliveryRepository.findById(100L)).thenReturn(Optional.of(delivery));
        when(ratingRepository.existsByDeliveryId(100L)).thenReturn(false);
        when(ratingRepository.save(any(DeliveryRating.class))).thenAnswer(i -> {
            DeliveryRating r = i.getArgument(0);
            r.setId(10L);
            return r;
        });

        DeliveryRatingResponse response = deliveryRatingService.createRating(request, camper);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(5, response.getRating());
        verify(ratingRepository, times(1)).save(any());
    }

    @Test
    void testCreateRating_ThrowsWhenNotDelivered() {
        delivery.setStatus(DeliveryStatus.IN_TRANSIT);
        when(deliveryRepository.findById(100L)).thenReturn(Optional.of(delivery));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            deliveryRatingService.createRating(request, camper);
        });

        assertTrue(exception.getMessage().contains("can only rate a completed delivery"));
    }
}

