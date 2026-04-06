package tn.esprit.projetpidev.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.dto.review.ReviewRequest;
import tn.esprit.projetpidev.dto.review.ReviewResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.EquipmentRepository;
import tn.esprit.projetpidev.repositories.ReviewRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private EquipmentRepository equipmentRepository;

    @InjectMocks
    private IReviewServiceImpl reviewService;

    private User camper;
    private Equipment equipment;
    private ReviewRequest request;

    @BeforeEach
    void setUp() {
        camper = new User();
        camper.setId(1L);

        equipment = Equipment.builder()
                .id(100L)
                .name("Sleeping Bag")
                .build();

        request = new ReviewRequest();
        request.setEquipmentId(100L);
        request.setRating(4);
        request.setComment("Very warm.");
    }

    @Test
    void testCreateReview_Success() {
        when(equipmentRepository.findById(100L)).thenReturn(Optional.of(equipment));
        when(reviewRepository.existsByEquipmentIdAndAuthorId(100L, 1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> {
            Review r = i.getArgument(0);
            r.setId(10L);
            return r;
        });

        ReviewResponse response = reviewService.createReview(request, camper);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(4, response.getRating());
        assertEquals("Very warm.", response.getComment());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testCreateReview_ThrowsWhenAlreadyReviewed() {
        when(equipmentRepository.findById(100L)).thenReturn(Optional.of(equipment));
        when(reviewRepository.existsByEquipmentIdAndAuthorId(100L, 1L)).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reviewService.createReview(request, camper);
        });

        assertTrue(exception.getMessage().contains("already reviewed this equipment"));
        verify(reviewRepository, never()).save(any(Review.class));
    }
}

