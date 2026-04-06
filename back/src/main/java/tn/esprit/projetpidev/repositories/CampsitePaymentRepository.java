// Module: Official Campsite & Booking | Layer: Repository
package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.projetpidev.domain.CampsitePayment;

import java.util.Optional;

public interface CampsitePaymentRepository extends JpaRepository<CampsitePayment, Long> {

    Optional<CampsitePayment> findByBooking_Id(Long bookingId);
}
