// Module: Official Campsite & Booking | Layer: Repository
package tn.esprit.projetpidev.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.projetpidev.domain.Campsite;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;
import tn.esprit.projetpidev.domain.enums.CampsiteType;

import java.math.BigDecimal;
import java.util.List;

public interface CampsiteRepository extends JpaRepository<Campsite, Long> {

    Page<Campsite> findByStatusNot(CampsiteStatus status, Pageable pageable);

    Page<Campsite> findByStatus(CampsiteStatus status, Pageable pageable);

    Page<Campsite> findByOwner_Id(Long ownerId, Pageable pageable);

    @Query("""
            SELECT c FROM Campsite c
            WHERE c.status IN (
                tn.esprit.projetpidev.domain.enums.CampsiteStatus.ACTIVE,
                tn.esprit.projetpidev.domain.enums.CampsiteStatus.FULL
              )
              AND (:country IS NULL OR LOWER(c.country) LIKE LOWER(CONCAT('%', :country, '%')))
              AND (:city    IS NULL OR LOWER(c.city)    LIKE LOWER(CONCAT('%', :city,    '%')))
              AND (:type    IS NULL OR c.type = :type)
              AND (:minPrice IS NULL OR c.pricePerNight >= :minPrice)
              AND (:maxPrice IS NULL OR c.pricePerNight <= :maxPrice)
            """)
    Page<Campsite> search(
            @Param("country")  String country,
            @Param("city")     String city,
            @Param("type")     CampsiteType type,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    List<Campsite> findByOwner_Id(Long ownerId);

    /** Used by scheduler: all non-deleted campsites */
    List<Campsite> findAllByStatusNot(CampsiteStatus status);

    /** Used by scheduler: campsites with specific statuses */
    List<Campsite> findAllByStatusIn(List<CampsiteStatus> statuses);
}
