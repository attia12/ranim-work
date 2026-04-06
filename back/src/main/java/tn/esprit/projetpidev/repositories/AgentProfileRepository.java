package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.userprofile.AgentProfile;
import java.util.Optional;

@Repository
public interface AgentProfileRepository extends JpaRepository<AgentProfile, Long> {
    Optional<AgentProfile> findByUserId(Long userId);
}
