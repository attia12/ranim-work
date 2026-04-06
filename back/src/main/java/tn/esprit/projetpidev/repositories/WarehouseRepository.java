package tn.esprit.projetpidev.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.Warehouse;
import java.util.List;
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    List<Warehouse> findByProviderProfileId(Long providerProfileId);
    List<Warehouse> findByProviderProfileUserId(Long userId);
}
