package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.Equipment;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByCategoryId(Long categoryId);
    List<Equipment> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    List<Equipment> findByNameContainingIgnoreCase(String keyword);
    boolean existsByCategoryId(Long categoryId);
}

