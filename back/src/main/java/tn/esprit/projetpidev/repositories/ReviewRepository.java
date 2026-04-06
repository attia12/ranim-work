package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.Review;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByEquipmentIdOrderByCreatedAtDesc(Long equipmentId);
    List<Review> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    Optional<Review> findByEquipmentIdAndAuthorId(Long equipmentId, Long authorId);
    boolean existsByEquipmentIdAndAuthorId(Long equipmentId, Long authorId);
}

