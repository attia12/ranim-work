// Module: Outdoor Campsite & Booking | Layer: Repository
package tn.esprit.projetpidev.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.projetpidev.domain.OutdoorBooking;

import java.time.LocalDate;

public interface OutdoorBookingRepository extends JpaRepository<OutdoorBooking, Long> {

    Page<OutdoorBooking> findByCamper_Id(Long camperId, Pageable pageable);

    Page<OutdoorBooking> findByOutdoorCampsite_Id(Long siteId, Pageable pageable);

    @Query("""
            SELECT COUNT(b) FROM OutdoorBooking b
            WHERE b.outdoorCampsite.id = :siteId
              AND b.status != tn.esprit.projetpidev.domain.enums.OutdoorBookingStatus.CANCELLED
              AND b.checkInDate  < :checkOut
              AND b.checkOutDate > :checkIn
            """)
    long countOverlapping(
            @Param("siteId")   Long siteId,
            @Param("checkIn")  LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}
