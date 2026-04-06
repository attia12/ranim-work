package tn.esprit.projetpidev.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.userprofile.CamperProfile;
import java.util.Optional;
@Repository
public interface CamperProfileRepository extends JpaRepository<CamperProfile, Long> {
    Optional<CamperProfile> findByUserId(Long userId);
}
