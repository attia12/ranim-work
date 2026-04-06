package tn.esprit.projetpidev.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.userprofile.CoachProfile;
import java.util.Optional;
@Repository
public interface CoachProfileRepository extends JpaRepository<CoachProfile, Long> {
    Optional<CoachProfile> findByUserId(Long userId);
}
