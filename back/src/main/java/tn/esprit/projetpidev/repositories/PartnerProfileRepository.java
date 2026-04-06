package tn.esprit.projetpidev.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.userprofile.PartnerProfile;
import java.util.Optional;
@Repository
public interface PartnerProfileRepository extends JpaRepository<PartnerProfile, Long> {
    Optional<PartnerProfile> findByUserId(Long userId);
}
