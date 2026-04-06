package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.dto.review.ReviewRequest;
import tn.esprit.projetpidev.dto.review.ReviewResponse;
import tn.esprit.projetpidev.domain.Equipment;
import tn.esprit.projetpidev.domain.Review;
import tn.esprit.projetpidev.repositories.EquipmentRepository;
import tn.esprit.projetpidev.repositories.ReviewRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class IReviewServiceImpl implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final EquipmentRepository equipmentRepository;

    @Override
    public ReviewResponse createReview(ReviewRequest request, User loggedInUser) {
        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", request.getEquipmentId()));

        if (reviewRepository.existsByEquipmentIdAndAuthorId(equipment.getId(), loggedInUser.getId())) {
            throw new IllegalStateException("You have already reviewed this equipment");
        }

        Review review = Review.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .equipment(equipment)
                .author(loggedInUser)
                .build();

        return mapToResponse(reviewRepository.save(review));
    }

    @Override
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request, User loggedInUser) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
        boolean isAuthor = review.getAuthor().getId().equals(loggedInUser.getId());
        boolean isAdmin  = loggedInUser.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new IllegalArgumentException("Only the author or an admin can update this review");
        }
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return mapToResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByEquipment(Long equipmentId) {
        return reviewRepository.findByEquipmentIdOrderByCreatedAtDesc(equipmentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByUser(Long userId) {
        return reviewRepository.findByAuthorIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void deleteReview(Long id, User loggedInUser) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));

        boolean isAuthor = review.getAuthor().getId().equals(loggedInUser.getId());
        boolean isAdmin = loggedInUser.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new IllegalArgumentException("Only the author or an admin can delete this review");
        }

        reviewRepository.deleteById(id);
    }

    private ReviewResponse mapToResponse(Review r) {
        ReviewResponse resp = new ReviewResponse();
        resp.setId(r.getId());
        resp.setRating(r.getRating());
        resp.setComment(r.getComment());
        resp.setEquipmentId(r.getEquipment().getId());
        resp.setEquipmentName(r.getEquipment().getName());
        resp.setAuthorId(r.getAuthor().getId());
        resp.setAuthorFullName(r.getAuthor().getFullname());
        resp.setCreatedAt(r.getCreatedAt());
        return resp;
    }
}
