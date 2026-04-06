// Module: Official Campsite & Booking | Layer: Service Implementation
package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.Availability;
import tn.esprit.projetpidev.domain.Campsite;
import tn.esprit.projetpidev.dto.availability.AvailabilityRequest;
import tn.esprit.projetpidev.dto.availability.AvailabilityResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.AvailabilityRepository;
import tn.esprit.projetpidev.repositories.CampsiteRepository;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class IAvailabilityServiceImpl implements IAvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final CampsiteRepository campsiteRepository;

    @Override
    public AvailabilityResponse create(AvailabilityRequest request) {
        Campsite campsite = campsiteRepository.findById(request.getCampsiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", request.getCampsiteId()));

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate.");
        }

        Availability availability = Availability.builder()
                .campsite(campsite)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .numberOfPlaces(request.getNumberOfPlaces())
                .weatherCondition(request.getWeatherCondition())
                .isBlocked(request.isBlocked())
                .build();

        return mapToResponse(availabilityRepository.save(availability));
    }

    @Override
    public AvailabilityResponse update(Long id, AvailabilityRequest request) {
        Availability availability = findById(id);
        availability.setStartDate(request.getStartDate());
        availability.setEndDate(request.getEndDate());
        availability.setNumberOfPlaces(request.getNumberOfPlaces());
        availability.setWeatherCondition(request.getWeatherCondition());
        availability.setBlocked(request.isBlocked());
        return mapToResponse(availabilityRepository.save(availability));
    }

    @Override
    public void delete(Long id) {
        availabilityRepository.delete(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getByCampsite(Long campsiteId) {
        return availabilityRepository.findByCampsite_Id(campsiteId)
                .stream().map(this::mapToResponse).toList();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Availability findById(Long id) {
        return availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability", id));
    }

    AvailabilityResponse mapToResponse(Availability a) {
        AvailabilityResponse r = new AvailabilityResponse();
        r.setId(a.getId());
        r.setCampsiteId(a.getCampsite().getId());
        r.setCampsiteName(a.getCampsite().getName());
        r.setStartDate(a.getStartDate());
        r.setEndDate(a.getEndDate());
        r.setNumberOfPlaces(a.getNumberOfPlaces());
        r.setWeatherCondition(a.getWeatherCondition());
        r.setBlocked(a.isBlocked());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }
}
