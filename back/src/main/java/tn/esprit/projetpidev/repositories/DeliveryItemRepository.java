package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.DeliveryItem;

import java.util.List;

@Repository
public interface DeliveryItemRepository extends JpaRepository<DeliveryItem, Long> {
    List<DeliveryItem> findByDeliveryId(Long deliveryId);
    void deleteByDeliveryId(Long deliveryId);
}

