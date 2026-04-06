package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.DeliveryRating;

import java.util.Optional;

@Repository
public interface DeliveryRatingRepository extends JpaRepository<DeliveryRating, Long> {
    Optional<DeliveryRating> findByDeliveryId(Long deliveryId);
    boolean existsByDeliveryId(Long deliveryId);
}

