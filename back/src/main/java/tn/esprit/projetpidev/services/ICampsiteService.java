// Module: Official Campsite & Booking | Layer: Service Interface
package tn.esprit.projetpidev.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.projetpidev.domain.enums.CampsiteType;
import tn.esprit.projetpidev.dto.campsite.CampsiteRequest;
import tn.esprit.projetpidev.dto.campsite.CampsiteResponse;

import java.math.BigDecimal;

public interface ICampsiteService {

    CampsiteResponse create(CampsiteRequest request, Long ownerId);

    CampsiteResponse update(Long id, CampsiteRequest request, Long requesterId);

    void delete(Long id, Long requesterId);

    CampsiteResponse getById(Long id);

    Page<CampsiteResponse> search(String country, String city, CampsiteType type,
                                   BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<CampsiteResponse> getByOwner(Long ownerId, Pageable pageable);

    Page<CampsiteResponse> getAll(Pageable pageable);

    CampsiteResponse suspend(Long id);

    CampsiteResponse activate(Long id);
}
