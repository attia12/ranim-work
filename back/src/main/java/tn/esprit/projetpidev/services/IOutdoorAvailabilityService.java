// Module: Outdoor Campsite & Booking | Layer: Service Interface
package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.outdooravailability.OutdoorAvailabilityRequest;
import tn.esprit.projetpidev.dto.outdooravailability.OutdoorAvailabilityResponse;

import java.util.List;

public interface IOutdoorAvailabilityService {

    OutdoorAvailabilityResponse create(OutdoorAvailabilityRequest request);

    OutdoorAvailabilityResponse update(Long id, OutdoorAvailabilityRequest request);

    void delete(Long id);

    List<OutdoorAvailabilityResponse> getBySite(Long outdoorCampsiteId);
}
