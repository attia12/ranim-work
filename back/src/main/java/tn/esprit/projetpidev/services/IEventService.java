package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.event.EventRequest;
import tn.esprit.projetpidev.dto.event.EventResponse;

import java.util.List;

public interface IEventService {

    EventResponse createEvent(EventRequest request);

    EventResponse getEventById(Long id);

    List<EventResponse> getAllEvents();

    List<EventResponse> getEventsByStatus(String status);

    List<EventResponse> getEventsByOrganizer(Long organizerId);

    List<EventResponse> searchEvents(String keyword);

    EventResponse updateEvent(Long id, EventRequest request);

    void deleteEvent(Long id);
}
