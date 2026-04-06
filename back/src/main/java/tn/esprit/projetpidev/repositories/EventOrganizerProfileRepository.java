package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.userprofile.EventOrganizerProfile;

import java.util.Optional;

@Repository
public interface EventOrganizerProfileRepository extends JpaRepository<EventOrganizerProfile, Long> {
    Optional<EventOrganizerProfile> findByUserId(Long userId);
}