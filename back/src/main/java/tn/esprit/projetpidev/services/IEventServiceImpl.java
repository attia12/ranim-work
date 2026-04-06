package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.Event;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.dto.event.EventRequest;
import tn.esprit.projetpidev.dto.event.EventResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.EventRepository;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class IEventServiceImpl implements IEventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public EventResponse createEvent(EventRequest request) {
        User organizer = userRepository.findById(request.getEvent_organizer_id())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getEvent_organizer_id()));

        Event event = Event.builder()
                .title(request.getTitle())
                .location(request.getLocation())
                .date(request.getDate())
                .maxParticipants(request.getMaxParticipants())
                .status(request.getStatus() != null ? request.getStatus() : "UPCOMING")
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .price(request.getPrice())
                .organizer(organizer)
                .build();

        return mapToResponse(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        return mapToResponse(eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByStatus(String status) {
        return eventRepository.findByStatus(status)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByOrganizer(Long organizerId) {
        return eventRepository.findByOrganizer_Id(organizerId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> searchEvents(String keyword) {
        return eventRepository.findByTitleContaining(keyword)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event existing = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));

        User organizer = userRepository.findById(request.getEvent_organizer_id())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getEvent_organizer_id()));

        existing.setTitle(request.getTitle());
        existing.setLocation(request.getLocation());
        existing.setDate(request.getDate());
        existing.setMaxParticipants(request.getMaxParticipants());
        existing.setStatus(request.getStatus());
        existing.setImageUrl(request.getImageUrl());
        existing.setCategory(request.getCategory());
        existing.setPrice(request.getPrice());
        existing.setOrganizer(organizer);

        return mapToResponse(eventRepository.save(existing));
    }

    @Override
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event", id);
        }
        eventRepository.deleteById(id);
    }

    // ── mapper ───────────────────────────────────────────────────────────────
    // Organizer fields accessed directly on User — same pattern as camperFullName in Delivery

    private EventResponse mapToResponse(Event e) {
        EventResponse r = new EventResponse();
        r.setEventId(e.getEventId());
        r.setTitle(e.getTitle());
        r.setLocation(e.getLocation());
        r.setDate(e.getDate());
        r.setMaxParticipants(e.getMaxParticipants());
        r.setStatus(e.getStatus());
        r.setImageUrl(e.getImageUrl());
        r.setCategory(e.getCategory());
        r.setPrice(e.getPrice());
        if (e.getOrganizer() != null) {
            r.setEventOrganizerId(e.getOrganizer().getId());
            r.setOrganizerFullName(e.getOrganizer().getFullname());
        }
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}

