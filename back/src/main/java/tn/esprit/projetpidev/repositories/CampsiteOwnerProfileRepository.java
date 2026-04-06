package tn.esprit.projetpidev.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.userprofile.CampsiteOwnerProfile;
import java.util.Optional;
@Repository
public interface CampsiteOwnerProfileRepository extends JpaRepository<CampsiteOwnerProfile, Long> {
    Optional<CampsiteOwnerProfile> findByUserId(Long userId);
}
