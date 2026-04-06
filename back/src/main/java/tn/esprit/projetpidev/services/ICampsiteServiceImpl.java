// Module: Official Campsite & Booking | Layer: Service Implementation
package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.Campsite;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;
import tn.esprit.projetpidev.domain.enums.CampsiteType;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.dto.campsite.CampsiteRequest;
import tn.esprit.projetpidev.dto.campsite.CampsiteResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.CampsiteRepository;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ICampsiteServiceImpl implements ICampsiteService {

    private final CampsiteRepository campsiteRepository;
    private final UserRepository userRepository;

    @Override
    public CampsiteResponse create(CampsiteRequest request, Long ownerId) {
        User owner = findUser(ownerId);
        assertOwnerOrAdmin(owner);

        Campsite campsite = Campsite.builder()
                .name(request.getName())
                .description(request.getDescription())
                .country(request.getCountry())
                .city(request.getCity())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .capacity(request.getCapacity())
                .type(request.getType())
                .pricePerNight(request.getPricePerNight())
                .pictures(request.getPictures())
                .amenities(request.getAmenities())
                .rules(request.getRules())
                .status(CampsiteStatus.ACTIVE)
                .owner(owner)
                .build();

        Campsite saved = campsiteRepository.save(campsite);
        log.info("Campsite created: id={}, name={}, owner={}", saved.getId(), saved.getName(), ownerId);
        return mapToResponse(saved);
    }

    @Override
    public CampsiteResponse update(Long id, CampsiteRequest request, Long requesterId) {
        Campsite campsite = findCampsite(id);
        User requester = findUser(requesterId);
        assertOwnerOrAdmin(requester);

        if (requester.getRole() != Role.ADMIN && !campsite.getOwner().getId().equals(requesterId)) {
            throw new IllegalStateException("You do not own this campsite.");
        }

        campsite.setName(request.getName());
        campsite.setDescription(request.getDescription());
        campsite.setCountry(request.getCountry());
        campsite.setCity(request.getCity());
        campsite.setAddress(request.getAddress());
        campsite.setLatitude(request.getLatitude());
        campsite.setLongitude(request.getLongitude());
        campsite.setCapacity(request.getCapacity());
        campsite.setType(request.getType());
        campsite.setPricePerNight(request.getPricePerNight());
        campsite.setPictures(request.getPictures());
        campsite.setAmenities(request.getAmenities());
        campsite.setRules(request.getRules());

        log.info("Campsite updated: id={}", id);
        return mapToResponse(campsiteRepository.save(campsite));
    }

    @Override
    public void delete(Long id, Long requesterId) {
        Campsite campsite = findCampsite(id);
        User requester = findUser(requesterId);
        assertOwnerOrAdmin(requester);

        if (requester.getRole() != Role.ADMIN && !campsite.getOwner().getId().equals(requesterId)) {
            throw new IllegalStateException("You do not own this campsite.");
        }

        campsite.setStatus(CampsiteStatus.DELETED);
        campsiteRepository.save(campsite);
        log.info("Campsite soft-deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CampsiteResponse getById(Long id) {
        return mapToResponse(findCampsite(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CampsiteResponse> search(String country, String city, CampsiteType type,
                                          BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return campsiteRepository.search(country, city, type, minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CampsiteResponse> getByOwner(Long ownerId, Pageable pageable) {
        return campsiteRepository.findByOwner_Id(ownerId, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CampsiteResponse> getAll(Pageable pageable) {
        return campsiteRepository.findByStatusNot(CampsiteStatus.DELETED, pageable).map(this::mapToResponse);
    }

    @Override
    public CampsiteResponse suspend(Long id) {
        Campsite campsite = findCampsite(id);
        campsite.setStatus(CampsiteStatus.SUSPENDED);
        log.info("Campsite suspended: id={}", id);
        return mapToResponse(campsiteRepository.save(campsite));
    }

    @Override
    public CampsiteResponse activate(Long id) {
        Campsite campsite = findCampsite(id);
        campsite.setStatus(CampsiteStatus.ACTIVE);
        log.info("Campsite activated: id={}", id);
        return mapToResponse(campsiteRepository.save(campsite));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Campsite findCampsite(Long id) {
        return campsiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private void assertOwnerOrAdmin(User user) {
        if (user.getRole() != Role.COMPSITEOWNERS && user.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Only OWNER or ADMIN can manage campsites.");
        }
    }

    CampsiteResponse mapToResponse(Campsite c) {
        CampsiteResponse r = new CampsiteResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setDescription(c.getDescription());
        r.setCountry(c.getCountry());
        r.setCity(c.getCity());
        r.setAddress(c.getAddress());
        r.setLatitude(c.getLatitude());
        r.setLongitude(c.getLongitude());
        r.setCapacity(c.getCapacity());
        r.setType(c.getType());
        r.setPricePerNight(c.getPricePerNight());
        r.setPictures(splitCsv(c.getPictures()));
        r.setAmenities(splitCsv(c.getAmenities()));
        r.setRules(c.getRules());
        r.setStatus(c.getStatus());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        if (c.getOwner() != null) {
            r.setOwnerId(c.getOwner().getId());
            r.setOwnerName(c.getOwner().getFullname());
        }
        return r;
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptyList();
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
