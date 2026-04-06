// Module: Outdoor Campsite & Booking | Layer: Service Implementation
package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.OutdoorAvailability;
import tn.esprit.projetpidev.domain.OutdoorCampsite;
import tn.esprit.projetpidev.dto.outdooravailability.OutdoorAvailabilityRequest;
import tn.esprit.projetpidev.dto.outdooravailability.OutdoorAvailabilityResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.OutdoorAvailabilityRepository;
import tn.esprit.projetpidev.repositories.OutdoorBookingRepository;
import tn.esprit.projetpidev.repositories.OutdoorCampsiteRepository;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class IOutdoorAvailabilityServiceImpl implements IOutdoorAvailabilityService {

    private final OutdoorAvailabilityRepository availabilityRepository;
    private final OutdoorCampsiteRepository outdoorCampsiteRepository;
    private final OutdoorBookingRepository outdoorBookingRepository;

    @Override
    public OutdoorAvailabilityResponse create(OutdoorAvailabilityRequest request) {
        OutdoorCampsite site = outdoorCampsiteRepository.findById(request.getOutdoorCampsiteId())
                .orElseThrow(() -> new ResourceNotFoundException("OutdoorCampsite", request.getOutdoorCampsiteId()));

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate.");
        }

        OutdoorAvailability availability = OutdoorAvailability.builder()
                .outdoorCampsite(site)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isAvailable(request.isAvailable())
                .note(request.getNote())
                .build();

        return mapToResponse(availabilityRepository.save(availability));
    }

    @Override
    public OutdoorAvailabilityResponse update(Long id, OutdoorAvailabilityRequest request) {
        OutdoorAvailability a = findById(id);
        a.setStartDate(request.getStartDate());
        a.setEndDate(request.getEndDate());
        a.setAvailable(request.isAvailable());
        a.setNote(request.getNote());
        return mapToResponse(availabilityRepository.save(a));
    }

    @Override
    public void delete(Long id) {
        availabilityRepository.delete(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutdoorAvailabilityResponse> getBySite(Long outdoorCampsiteId) {
        return availabilityRepository.findByOutdoorCampsite_Id(outdoorCampsiteId)
                .stream().map(a -> mapToResponseWithBookingCheck(a, outdoorCampsiteId)).toList();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private OutdoorAvailability findById(Long id) {
        return availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OutdoorAvailability", id));
    }

    private OutdoorAvailabilityResponse mapToResponseWithBookingCheck(OutdoorAvailability a, Long siteId) {
        OutdoorAvailabilityResponse r = mapToResponse(a);
        // isFullyBooked: any overlapping confirmed booking means "fully booked" for that period
        long overlapping = outdoorBookingRepository.countOverlapping(siteId, a.getStartDate(), a.getEndDate());
        r.setFullyBooked(overlapping > 0);
        return r;
    }

    OutdoorAvailabilityResponse mapToResponse(OutdoorAvailability a) {
        OutdoorAvailabilityResponse r = new OutdoorAvailabilityResponse();
        r.setId(a.getId());
        r.setOutdoorCampsiteId(a.getOutdoorCampsite().getId());
        r.setOutdoorCampsiteName(a.getOutdoorCampsite().getName());
        r.setStartDate(a.getStartDate());
        r.setEndDate(a.getEndDate());
        r.setAvailable(a.isAvailable());
        r.setNote(a.getNote());
        return r;
    }
}
