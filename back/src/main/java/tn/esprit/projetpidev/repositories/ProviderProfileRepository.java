package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.userprofile.ProviderProfile;
import java.util.Optional;

@Repository
public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, Long> {
    Optional<ProviderProfile> findByUserId(Long userId);
}
