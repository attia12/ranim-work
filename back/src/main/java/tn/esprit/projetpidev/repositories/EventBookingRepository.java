package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.EventBooking;

import java.util.List;

@Repository
public interface EventBookingRepository extends JpaRepository<EventBooking, Long> {

    // FIXED: all three methods below were broken (tried to traverse .userId on a relation)
    List<EventBooking> findByUser_Id(Long userId);

    List<EventBooking> findByEvent_EventId(Long eventId);

    boolean existsByEvent_EventIdAndUser_Id(Long eventId, Long userId);

    int countByEvent_EventId(Long eventId);
}
