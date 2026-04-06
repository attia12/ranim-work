// Module: Outdoor Campsite & Booking | Layer: Service Interface
package tn.esprit.projetpidev.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.projetpidev.dto.outdoorbooking.OutdoorBookingRequest;
import tn.esprit.projetpidev.dto.outdoorbooking.OutdoorBookingResponse;

public interface IOutdoorBookingService {

    OutdoorBookingResponse create(OutdoorBookingRequest request, Long camperId);

    OutdoorBookingResponse getById(Long id);

    Page<OutdoorBookingResponse> getMyBookings(Long camperId, Pageable pageable);

    Page<OutdoorBookingResponse> getBySite(Long siteId, Pageable pageable);

    OutdoorBookingResponse cancel(Long id, Long requesterId);
}
