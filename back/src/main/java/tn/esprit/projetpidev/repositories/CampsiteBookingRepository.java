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

    /** Total bookings count. */
    long count();

    /** Count by status. */
    long countByStatus(CampsiteBookingStatus status);

    /** Occupancy stats per campsite: [campsiteId, name, total, confirmed, cancelled] */
    @Query(value = """
            SELECT c.id, c.name,
                   COUNT(b.id)                                              AS total,
                   SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END) AS confirmed,
                   SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled
            FROM campsites c
            LEFT JOIN campsite_bookings b ON c.id = b.campsite_id
            GROUP BY c.id, c.name
            ORDER BY total DESC
            """, nativeQuery = true)
    List<Object[]> occupancyPerCampsite();

    /** Fraud suspects: users with >= :minCancellations cancelled bookings. */
    @Query(value = """
            SELECT u.id, u.email, u.first_name, u.last_name,
                   COUNT(b.id)                                                 AS cancellations,
                   (SELECT COUNT(*) FROM campsite_bookings b2 WHERE b2.camper_id = u.id) AS total_bookings
            FROM users u
            JOIN campsite_bookings b ON b.camper_id = u.id
            WHERE b.status = 'CANCELLED'
            GROUP BY u.id, u.email, u.first_name, u.last_name
            HAVING cancellations >= :minCancellations
            ORDER BY cancellations DESC
            """, nativeQuery = true)
    List<Object[]> fraudSuspects(@Param("minCancellations") int minCancellations);

    /** All bookings for CSV export with key fields. */
    @Query(value = """
            SELECT b.id, u.email, c.name, b.check_in_date, b.check_out_date,
                   b.number_of_guests, b.total_price, b.status, b.created_at
            FROM campsite_bookings b
            JOIN users u ON b.camper_id = u.id
            JOIN campsites c ON b.campsite_id = c.id
            ORDER BY b.created_at DESC
            """, nativeQuery = true)
    List<Object[]> allBookingsForExport();
}
