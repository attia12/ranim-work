// Module: Outdoor Campsite & Booking | Layer: Service Implementation
package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.OutdoorBooking;
import tn.esprit.projetpidev.domain.OutdoorCampsite;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.enums.OutdoorBookingStatus;
import tn.esprit.projetpidev.domain.enums.OutdoorCampsiteStatus;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.dto.outdoorbooking.OutdoorBookingRequest;
import tn.esprit.projetpidev.dto.outdoorbooking.OutdoorBookingResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.OutdoorBookingRepository;
import tn.esprit.projetpidev.repositories.OutdoorCampsiteRepository;
import tn.esprit.projetpidev.repositories.UserRepository;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class IOutdoorBookingServiceImpl implements IOutdoorBookingService {

    private final OutdoorBookingRepository bookingRepository;
    private final OutdoorCampsiteRepository siteRepository;
    private final UserRepository userRepository;

    @Override
    public OutdoorBookingResponse create(OutdoorBookingRequest request, Long camperId) {
        OutdoorCampsite site = siteRepository.findById(request.getOutdoorCampsiteId())
                .orElseThrow(() -> new ResourceNotFoundException("OutdoorCampsite", request.getOutdoorCampsiteId()));
        User camper = userRepository.findById(camperId)
                .orElseThrow(() -> new ResourceNotFoundException("User", camperId));

        if (site.getStatus() != OutdoorCampsiteStatus.APPROVED) {
            throw new IllegalStateException("Outdoor campsite is not available for booking.");
        }
        if (!request.getCheckInDate().isBefore(request.getCheckOutDate())) {
            throw new IllegalArgumentException("checkInDate must be before checkOutDate.");
        }

        // Overlap check
        long overlapping = bookingRepository.countOverlapping(
                site.getId(), request.getCheckInDate(), request.getCheckOutDate());
        if (overlapping > 0) {
            throw new IllegalStateException("Outdoor campsite is already booked for the requested period.");
        }

        OutdoorBooking booking = OutdoorBooking.builder()
                .outdoorCampsite(site)
                .camper(camper)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .numberOfGuests(request.getNumberOfGuests())
                .status(OutdoorBookingStatus.CONFIRMED)
                .build();

        OutdoorBooking saved = bookingRepository.save(booking);
        log.info("OutdoorBooking confirmed: id={}, site={}, camper={}", saved.getId(), site.getId(), camperId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OutdoorBookingResponse getById(Long id) {
        return mapToResponse(findBooking(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OutdoorBookingResponse> getMyBookings(Long camperId, Pageable pageable) {
        return bookingRepository.findByCamper_Id(camperId, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OutdoorBookingResponse> getBySite(Long siteId, Pageable pageable) {
        return bookingRepository.findByOutdoorCampsite_Id(siteId, pageable).map(this::mapToResponse);
    }

    @Override
    public OutdoorBookingResponse cancel(Long id, Long requesterId) {
        OutdoorBooking booking = findBooking(id);
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", requesterId));

        boolean isCamper = booking.getCamper().getId().equals(requesterId);
        boolean isAdmin   = requester.getRole() == Role.ADMIN;

        if (!isCamper && !isAdmin) {
            throw new IllegalStateException("Only the booking camper or ADMIN can cancel.");
        }

        booking.setStatus(OutdoorBookingStatus.CANCELLED);
        log.info("OutdoorBooking cancelled: id={}", id);
        return mapToResponse(bookingRepository.save(booking));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private OutdoorBooking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OutdoorBooking", id));
    }

    OutdoorBookingResponse mapToResponse(OutdoorBooking b) {
        OutdoorBookingResponse r = new OutdoorBookingResponse();
        r.setId(b.getId());
        r.setOutdoorCampsiteId(b.getOutdoorCampsite().getId());
        r.setOutdoorCampsiteName(b.getOutdoorCampsite().getName());
        r.setOutdoorCampsiteCountry(b.getOutdoorCampsite().getCountry());
        r.setOutdoorCampsiteCity(b.getOutdoorCampsite().getCity());
        r.setCamperId(b.getCamper().getId());
        r.setCamperFullName(b.getCamper().getFullname());
        r.setCheckInDate(b.getCheckInDate());
        r.setCheckOutDate(b.getCheckOutDate());
        r.setNumberOfGuests(b.getNumberOfGuests());
        r.setStatus(b.getStatus());
        r.setCreatedAt(b.getCreatedAt());
        r.setUpdatedAt(b.getUpdatedAt());
        return r;
    }
}
