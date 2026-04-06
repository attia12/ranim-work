package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.deliveryrating.DeliveryRatingRequest;
import tn.esprit.projetpidev.dto.deliveryrating.DeliveryRatingResponse;
import tn.esprit.projetpidev.domain.User;

public interface IDeliveryRatingService {
    DeliveryRatingResponse createRating(DeliveryRatingRequest request, User loggedInUser);
    DeliveryRatingResponse updateRating(Long ratingId, DeliveryRatingRequest request, User loggedInUser);
    DeliveryRatingResponse getRatingByDelivery(Long deliveryId);
    void deleteRating(Long id, User loggedInUser);
}

