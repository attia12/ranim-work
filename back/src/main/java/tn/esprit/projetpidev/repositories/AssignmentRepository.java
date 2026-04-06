package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.Assignment;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByEvent_EventId(Long eventId);

    /** All assignments for a guide (User with GUIDE role) */
    List<Assignment> findByGuide_Id(Long guideUserId);

    boolean existsByEvent_EventIdAndGuide_Id(Long eventId, Long guideUserId);

    List<Assignment> findByStatus(String status);
}
