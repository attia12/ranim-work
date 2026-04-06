package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.dto.deliveryrating.DeliveryRatingRequest;
import tn.esprit.projetpidev.dto.deliveryrating.DeliveryRatingResponse;
import tn.esprit.projetpidev.domain.Delivery;
import tn.esprit.projetpidev.domain.DeliveryRating;
import tn.esprit.projetpidev.domain.enums.DeliveryStatus;
import tn.esprit.projetpidev.repositories.DeliveryRatingRepository;
import tn.esprit.projetpidev.repositories.DeliveryRepository;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;

@Service
@Transactional
@RequiredArgsConstructor
public class IDeliveryRatingServiceImpl implements IDeliveryRatingService {

    private final DeliveryRatingRepository ratingRepository;
    private final DeliveryRepository deliveryRepository;

    @Override
    public DeliveryRatingResponse createRating(DeliveryRatingRequest request, User loggedInUser) {
        Delivery delivery = deliveryRepository.findById(request.getDeliveryId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", request.getDeliveryId()));

        if (delivery.getStatus() != DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("You can only rate a completed delivery");
        }
        if (!delivery.getCamper().getId().equals(loggedInUser.getId())) {
            throw new IllegalArgumentException("Only the camper of this delivery can rate it");
        }
        if (ratingRepository.existsByDeliveryId(delivery.getId())) {
            throw new IllegalStateException("This delivery has already been rated");
        }

        DeliveryRating rating = DeliveryRating.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .delivery(delivery)
                .author(loggedInUser)
                .build();

        return mapToResponse(ratingRepository.save(rating));
    }

    @Override
    public DeliveryRatingResponse updateRating(Long ratingId, DeliveryRatingRequest request, User loggedInUser) {
        DeliveryRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating", ratingId));
        boolean isAuthor = rating.getAuthor().getId().equals(loggedInUser.getId());
        boolean isAdmin  = loggedInUser.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new IllegalArgumentException("Only the author or an admin can update this rating");
        }
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        return mapToResponse(ratingRepository.save(rating));
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryRatingResponse getRatingByDelivery(Long deliveryId) {
        DeliveryRating rating = ratingRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating for delivery", deliveryId));
        return mapToResponse(rating);
    }

    @Override
    public void deleteRating(Long id, User loggedInUser) {
        DeliveryRating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating", id));

        boolean isAuthor = rating.getAuthor().getId().equals(loggedInUser.getId());
        boolean isAdmin = loggedInUser.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new IllegalArgumentException("Only the author or an admin can delete this rating");
        }

        ratingRepository.deleteById(id);
    }

    private DeliveryRatingResponse mapToResponse(DeliveryRating r) {
        DeliveryRatingResponse resp = new DeliveryRatingResponse();
        resp.setId(r.getId());
        resp.setRating(r.getRating());
        resp.setComment(r.getComment());
        resp.setDeliveryId(r.getDelivery().getId());
        resp.setAuthorId(r.getAuthor().getId());
        resp.setAuthorFullName(r.getAuthor().getFullname());
        resp.setCreatedAt(r.getCreatedAt());
        return resp;
    }
}

