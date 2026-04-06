// Module: Official Campsite & Booking | Layer: Service Interface
package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.availability.AvailabilityRequest;
import tn.esprit.projetpidev.dto.availability.AvailabilityResponse;

import java.util.List;

public interface IAvailabilityService {

    AvailabilityResponse create(AvailabilityRequest request);

    AvailabilityResponse update(Long id, AvailabilityRequest request);

    void delete(Long id);

    List<AvailabilityResponse> getByCampsite(Long campsiteId);
}
