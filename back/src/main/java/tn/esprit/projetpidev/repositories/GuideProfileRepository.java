package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.userprofile.GuideProfile;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuideProfileRepository extends JpaRepository<GuideProfile, Long> {

    Optional<GuideProfile> findByUserId(Long userId);

    List<GuideProfile> findByIsVerified(Boolean isVerified);
}
