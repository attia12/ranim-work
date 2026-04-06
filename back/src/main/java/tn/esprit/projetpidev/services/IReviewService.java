package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.dto.review.ReviewRequest;
import tn.esprit.projetpidev.dto.review.ReviewResponse;

import java.util.List;

public interface IReviewService {
    ReviewResponse createReview(ReviewRequest request, User loggedInUser);

    ReviewResponse updateReview(Long reviewId, ReviewRequest request, User loggedInUser);

    List<ReviewResponse> getReviewsByEquipment(Long equipmentId);

    List<ReviewResponse> getReviewsByUser(Long userId);

    List<ReviewResponse> getAllReviews();

    void deleteReview(Long id, User loggedInUser);
}
