package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.Delivery;
import tn.esprit.projetpidev.domain.enums.DeliveryStatus;

import java.util.Optional;
import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByCamperIdOrderByCreatedAtDesc(Long camperId);
    List<Delivery> findByStatus(DeliveryStatus status);
    List<Delivery> findByVehicleId(Long vehicleId);
    List<Delivery> findByVehicleOwnerId(Long ownerId);
    Optional<Delivery> findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
}

