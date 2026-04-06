package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.event.EventRequest;
import tn.esprit.projetpidev.dto.event.EventResponse;
import tn.esprit.projetpidev.services.IEventService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "CRUD operations for camping events")
public class EventController {

    private final IEventService eventService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EVENT_ORGANIZER', 'ADMIN')")
    public ResponseEntity<EventResponse> create(
            @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAll() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventResponse>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(eventService.getEventsByStatus(status));
    }

    @GetMapping("/organizer/{organizerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponse>> getByOrganizer(@PathVariable Long organizerId) {
        return ResponseEntity.ok(eventService.getEventsByOrganizer(organizerId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(eventService.searchEvents(keyword));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EVENT_ORGANIZER', 'ADMIN')")
    public ResponseEntity<EventResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EVENT_ORGANIZER', 'ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok("Event deleted successfully.");
    }
}
