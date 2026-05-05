// Module: Official Campsite & Booking | Layer: Repository
package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.projetpidev.domain.Availability;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByCampsite_Id(Long campsiteId);

    void deleteByCampsite_Id(Long campsiteId);

    @Query("""
            SELECT a FROM Availability a
            WHERE a.campsite.id = :campsiteId
              AND a.isBlocked = false
              AND a.startDate <= :endDate
              AND a.endDate   >= :startDate
            """)
    List<Availability> findAvailableSlots(
            @Param("campsiteId") Long campsiteId,
            @Param("startDate")  LocalDate startDate,
            @Param("endDate")    LocalDate endDate
    );
}
