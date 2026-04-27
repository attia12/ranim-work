// Module: Official Campsite & Booking | Layer: Repository
package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.projetpidev.domain.CampsiteStatusHistory;

import java.util.List;

public interface CampsiteStatusHistoryRepository extends JpaRepository<CampsiteStatusHistory, Long> {

    List<CampsiteStatusHistory> findByCampsite_IdOrderByChangedAtDesc(Long campsiteId);
}
