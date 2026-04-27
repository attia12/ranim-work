// Module: Official Campsite & Booking | Layer: Repository
package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.projetpidev.domain.CampsitePayment;

import java.util.List;
import java.util.Optional;

public interface CampsitePaymentRepository extends JpaRepository<CampsitePayment, Long> {

    Optional<CampsitePayment> findByBooking_Id(Long bookingId);

    /** Revenue grouped by month (last 12 months), newest first. */
    @Query(value = """
            SELECT DATE_FORMAT(p.paid_at, '%Y-%m') AS month, SUM(p.amount) AS revenue
            FROM campsite_payments p
            WHERE p.status = 'PAID'
            GROUP BY month
            ORDER BY month DESC
            LIMIT 12
            """, nativeQuery = true)
    List<Object[]> revenueByMonth();

    /** Total revenue of all PAID payments. */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM CampsitePayment p WHERE p.status = tn.esprit.projetpidev.domain.enums.CampsitePaymentStatus.PAID")
    java.math.BigDecimal totalRevenue();
}
