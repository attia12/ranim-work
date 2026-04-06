// Module: Outdoor Campsite & Booking | Layer: Repository
package tn.esprit.projetpidev.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.projetpidev.domain.OutdoorCampsite;
import tn.esprit.projetpidev.domain.enums.OutdoorCampsiteStatus;

import java.util.List;

public interface OutdoorCampsiteRepository extends JpaRepository<OutdoorCampsite, Long> {

    Page<OutdoorCampsite> findByStatus(OutdoorCampsiteStatus status, Pageable pageable);

    Page<OutdoorCampsite> findByProposedBy_Id(Long userId, Pageable pageable);

    List<OutdoorCampsite> findByProposedBy_Id(Long userId);
}
