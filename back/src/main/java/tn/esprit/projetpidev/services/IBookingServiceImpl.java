package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.Event;
import tn.esprit.projetpidev.domain.EventBooking;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.dto.booking.BookingRequest;
import tn.esprit.projetpidev.dto.booking.BookingResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.EventBookingRepository;
import tn.esprit.projetpidev.repositories.EventRepository;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class IBookingServiceImpl implements IBookingService {

    private final EventBookingRepository eventBookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public BookingResponse createBooking(BookingRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", request.getEventId()));

        // User resolved directly — same as Delivery.camper
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        // Guard: no duplicate booking
        if (eventBookingRepository.existsByEvent_EventIdAndUser_Id(
                event.getEventId(), user.getId())) {
            throw new RuntimeException(
                    "User " + user.getId() + " has already booked event " + event.getEventId());
        }

        // Guard: capacity check
        if (event.getMaxParticipants() != null) {
            int currentCount = eventBookingRepository.countByEvent_EventId(event.getEventId());
            if (currentCount >= event.getMaxParticipants()) {
                throw new RuntimeException(
                        "Event \"" + event.getTitle() + "\" is fully booked.");
            }
        }

        EventBooking booking = EventBooking.builder()
                .event(event)
                .user(user)
                .status("PENDING")
                .build();

        return mapToResponse(eventBookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId) {
        return mapToResponse(eventBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUser(Long userId) {
        return eventBookingRepository.findByUser_Id(userId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByEvent(Long eventId) {
        return eventBookingRepository.findByEvent_EventId(eventId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public BookingResponse updateStatus(Long bookingId, String status) {
        EventBooking booking = eventBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        booking.setStatus(status);
        return mapToResponse(eventBookingRepository.save(booking));
    }

    @Override
    public void cancelBooking(Long bookingId) {
        EventBooking booking = eventBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        booking.setStatus("CANCELLED");
        eventBookingRepository.save(booking);
    }

    // ── mapper ───────────────────────────────────────────────────────────────
    // User fields accessed directly — same as DeliveryResponse.camperFullName

    private BookingResponse mapToResponse(EventBooking b) {
        BookingResponse r = new BookingResponse();
        r.setId(b.getId());
        r.setEventId(b.getEvent().getEventId());
        r.setEventTitle(b.getEvent().getTitle());
        r.setEventDate(b.getEvent().getDate());
        r.setEventLocation(b.getEvent().getLocation());
        r.setUserId(b.getUser().getId());
        r.setStatus(b.getStatus());
        r.setBookingDate(b.getBookingDate());
        return r;
    }
}
