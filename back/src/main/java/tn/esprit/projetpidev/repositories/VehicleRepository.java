package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.Vehicle;
import tn.esprit.projetpidev.domain.enums.AvailabilityStatus;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    boolean existsByPlate(String plate);
    List<Vehicle> findByOwnerId(Long ownerId);
    List<Vehicle> findByIsVerifiedTrueAndAvailabilityStatus(AvailabilityStatus status);
}

