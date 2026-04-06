// Module: Official Campsite & Booking | Layer: Service Interface
package tn.esprit.projetpidev.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.projetpidev.dto.campsitebooking.CampsiteBookingRequest;
import tn.esprit.projetpidev.dto.campsitebooking.CampsiteBookingResponse;

public interface ICampsiteBookingService {

    CampsiteBookingResponse create(CampsiteBookingRequest request, Long camperId);

    CampsiteBookingResponse getById(Long id);

    Page<CampsiteBookingResponse> getMyCamperBookings(Long camperId, Pageable pageable);

    Page<CampsiteBookingResponse> getByCampsite(Long campsiteId, Pageable pageable);

    Page<CampsiteBookingResponse> getAll(Pageable pageable);

    CampsiteBookingResponse cancel(Long id, Long requesterId, String reason);

    CampsiteBookingResponse confirm(Long id);

    CampsiteBookingResponse complete(Long id);
}
