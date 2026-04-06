// Module: Outdoor Campsite & Booking | Layer: Repository
package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.projetpidev.domain.OutdoorAvailability;

import java.time.LocalDate;
import java.util.List;

public interface OutdoorAvailabilityRepository extends JpaRepository<OutdoorAvailability, Long> {

    List<OutdoorAvailability> findByOutdoorCampsite_Id(Long outdoorCampsiteId);

    @Query("""
            SELECT a FROM OutdoorAvailability a
            WHERE a.outdoorCampsite.id = :siteId
              AND a.isAvailable = true
              AND a.startDate <= :endDate
              AND a.endDate   >= :startDate
            """)
    List<OutdoorAvailability> findAvailable(
            @Param("siteId")    Long siteId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate
    );
}
