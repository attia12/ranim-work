package tn.esprit.projetpidev.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.userprofile.SponsorProfile;
import java.util.Optional;
@Repository
public interface SponsorProfileRepository extends JpaRepository<SponsorProfile, Long> {
    Optional<SponsorProfile> findByUserId(Long userId);
}
