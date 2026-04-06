package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.Groupe;
import java.util.List;

@Repository
public interface GroupeRepository extends JpaRepository<Groupe, Long> {
    List<Groupe> findByIsPrivate(boolean isPrivate);
    boolean existsByName(String name);
}