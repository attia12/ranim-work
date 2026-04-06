// Module: Official Campsite & Booking | Layer: Repository
package tn.esprit.projetpidev.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.projetpidev.domain.CampsiteBooking;
import tn.esprit.projetpidev.domain.enums.CampsiteBookingStatus;

import java.time.LocalDate;
import java.util.List;

public interface CampsiteBookingRepository extends JpaRepository<CampsiteBooking, Long> {

    Page<CampsiteBooking> findByCamper_Id(Long camperId, Pageable pageable);

    Page<CampsiteBooking> findByCampsite_Id(Long campsiteId, Pageable pageable);

    Page<CampsiteBooking> findAll(Pageable pageable);

    /** Counts active (non-cancelled) bookings overlapping the requested date range. */
    @Query("""
            SELECT COUNT(b) FROM CampsiteBooking b
            WHERE b.campsite.id = :campsiteId
              AND b.status NOT IN (tn.esprit.projetpidev.domain.enums.CampsiteBookingStatus.CANCELLED)
              AND b.checkInDate  < :checkOut
              AND b.checkOutDate > :checkIn
            """)
    long countOverlapping(
            @Param("campsiteId") Long campsiteId,
            @Param("checkIn")    LocalDate checkIn,
            @Param("checkOut")   LocalDate checkOut
    );

    /** Sum of guests in active overlapping bookings. */
    @Query("""
            SELECT COALESCE(SUM(b.numberOfGuests), 0) FROM CampsiteBooking b
            WHERE b.campsite.id = :campsiteId
              AND b.status NOT IN (tn.esprit.projetpidev.domain.enums.CampsiteBookingStatus.CANCELLED)
              AND b.checkInDate  < :checkOut
              AND b.checkOutDate > :checkIn
            """)
    int sumGuestsOverlapping(
            @Param("campsiteId") Long campsiteId,
            @Param("checkIn")    LocalDate checkIn,
            @Param("checkOut")   LocalDate checkOut
    );

    List<CampsiteBooking> findByCampsite_IdAndStatus(Long campsiteId, CampsiteBookingStatus status);
}
