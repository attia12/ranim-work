package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.Event;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // FIXED: was findByOrganizerId (broken) → must use underscore to traverse organizer.id
    List<Event> findByOrganizer_Id(Long organizerId);

    List<Event> findByStatus(String status);

    // ADDED: needed by search endpoint
    List<Event> findByTitleContaining(String keyword);
}
