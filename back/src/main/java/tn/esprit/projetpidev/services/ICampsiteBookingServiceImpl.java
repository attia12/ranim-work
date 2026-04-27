// Module: Official Campsite & Booking | Layer: Service Implementation
package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.Campsite;
import tn.esprit.projetpidev.domain.CampsiteBooking;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.enums.CampsiteBookingStatus;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.dto.campsitebooking.CampsiteBookingRequest;
import tn.esprit.projetpidev.dto.campsitebooking.CampsiteBookingResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.dto.notification.NotificationPayload;
import tn.esprit.projetpidev.repositories.CampsiteBookingRepository;
import tn.esprit.projetpidev.repositories.CampsiteRepository;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ICampsiteBookingServiceImpl implements ICampsiteBookingService {

    private final CampsiteBookingRepository bookingRepository;
    private final CampsiteRepository campsiteRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final WsNotificationService wsNotificationService;

    @Override
    public CampsiteBookingResponse create(CampsiteBookingRequest request, Long camperId) {
        Campsite campsite = campsiteRepository.findById(request.getCampsiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", request.getCampsiteId()));
        User camper = userRepository.findById(camperId)
                .orElseThrow(() -> new ResourceNotFoundException("User", camperId));

        LocalDate checkIn  = request.getCheckInDate();
        LocalDate checkOut = request.getCheckOutDate();

        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("checkInDate must be before checkOutDate.");
        }
        if (!checkIn.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("checkInDate must be in the future.");
        }

        // Capacity check: total booked guests in the period must not exceed capacity
        int occupiedGuests = bookingRepository.sumGuestsOverlapping(campsite.getId(), checkIn, checkOut);
        int capacity = campsite.getCapacity() != null ? campsite.getCapacity() : Integer.MAX_VALUE;
        if (occupiedGuests + request.getNumberOfGuests() > capacity) {
            throw new IllegalStateException("Not enough capacity for the requested period.");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal total = campsite.getPricePerNight() != null
                ? campsite.getPricePerNight().multiply(BigDecimal.valueOf(nights))
                : BigDecimal.ZERO;

        CampsiteBooking booking = CampsiteBooking.builder()
                .campsite(campsite)
                .camper(camper)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .numberOfGuests(request.getNumberOfGuests())
                .totalPrice(total)
                .status(CampsiteBookingStatus.PENDING)
                .build();

        CampsiteBooking saved = bookingRepository.save(booking);
        log.info("CampsiteBooking created: id={}, campsite={}, camper={}", saved.getId(), campsite.getId(), camperId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CampsiteBookingResponse getById(Long id) {
        return mapToResponse(findBooking(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CampsiteBookingResponse> getMyCamperBookings(Long camperId, Pageable pageable) {
        return bookingRepository.findByCamper_Id(camperId, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CampsiteBookingResponse> getByCampsite(Long campsiteId, Pageable pageable) {
        return bookingRepository.findByCampsite_Id(campsiteId, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CampsiteBookingResponse> getAll(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public CampsiteBookingResponse cancel(Long id, Long requesterId, String reason) {
        CampsiteBooking booking = findBooking(id);
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", requesterId));

        boolean isCamper = booking.getCamper().getId().equals(requesterId);
        boolean isAdmin   = requester.getRole() == Role.ADMIN;

        if (!isCamper && !isAdmin) {
            throw new IllegalStateException("Only the booking camper or ADMIN can cancel.");
        }
        if (booking.getStatus() == CampsiteBookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled.");
        }

        // Cancellation allowed only if checkInDate > today + 2 days
        if (!booking.getCheckInDate().isAfter(LocalDate.now().plusDays(2))) {
            throw new IllegalStateException("Cancellation not allowed within 2 days of check-in.");
        }

        booking.setStatus(CampsiteBookingStatus.CANCELLED);
        booking.setCancellationReason(reason);

        // TODO: trigger refund logic via payment stub service
        log.info("CampsiteBooking cancelled: id={}, reason={}", id, reason);
        CampsiteBookingResponse response = mapToResponse(bookingRepository.save(booking));

        wsNotificationService.sendToUser(
                booking.getCamper().getId(),
                NotificationPayload.builder()
                        .type("CAMPSITE_BOOKING_CANCELLED")
                        .message("Your campsite booking #" + id + " has been cancelled.")
                        .referenceId(id)
                        .build()
        );
        return response;
    }

    @Override
    public CampsiteBookingResponse confirm(Long id) {
        CampsiteBooking booking = findBooking(id);
        booking.setStatus(CampsiteBookingStatus.CONFIRMED);
        CampsiteBooking saved = bookingRepository.save(booking);

        // Send confirmation email
        try {
            emailService.sendBookingConfirmationEmail(
                    booking.getCamper().getEmail(),
                    booking.getCamper().getFullname(),
                    booking.getCampsite().getName(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getTotalPrice()
            );
        } catch (Exception e) {
            log.warn("Failed to send booking confirmation email for booking {}: {}", id, e.getMessage());
        }

        wsNotificationService.sendToUser(
                booking.getCamper().getId(),
                NotificationPayload.builder()
                        .type("CAMPSITE_BOOKING_CONFIRMED")
                        .message("Your campsite booking #" + id + " has been confirmed.")
                        .referenceId(id)
                        .build()
        );

        log.info("CampsiteBooking confirmed: id={}", id);
        return mapToResponse(saved);
    }

    @Override
    public CampsiteBookingResponse complete(Long id) {
        CampsiteBooking booking = findBooking(id);
        booking.setStatus(CampsiteBookingStatus.COMPLETED);
        log.info("CampsiteBooking completed: id={}", id);
        return mapToResponse(bookingRepository.save(booking));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private CampsiteBooking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CampsiteBooking", id));
    }

    CampsiteBookingResponse mapToResponse(CampsiteBooking b) {
        CampsiteBookingResponse r = new CampsiteBookingResponse();
        r.setId(b.getId());
        r.setCampsiteId(b.getCampsite().getId());
        r.setCampsiteName(b.getCampsite().getName());
        r.setCampsiteCountry(b.getCampsite().getCountry());
        r.setCampsiteCity(b.getCampsite().getCity());
        r.setCamperId(b.getCamper().getId());
        r.setCamperFullName(b.getCamper().getFullname());
        r.setCheckInDate(b.getCheckInDate());
        r.setCheckOutDate(b.getCheckOutDate());
        r.setNumberOfGuests(b.getNumberOfGuests());
        r.setTotalPrice(b.getTotalPrice());
        r.setStatus(b.getStatus());
        r.setCancellationReason(b.getCancellationReason());
        r.setCreatedAt(b.getCreatedAt());
        r.setUpdatedAt(b.getUpdatedAt());
        return r;
    }
}
