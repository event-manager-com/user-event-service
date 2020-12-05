package gregad.eventmanager.usereventservice.services.event_service;

import gregad.eventmanager.usereventservice.dto.EventDto;
import gregad.eventmanager.usereventservice.dto.EventResponseDto;
import gregad.eventmanager.usereventservice.dto.SocialNetworkConnectionsDto;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Greg Adler
 */
public interface EventService {
    List<SocialNetworkConnectionsDto> getAllConnections(int id);
    EventResponseDto createEvent(EventDto event);
    EventResponseDto updateEvent(int ownerId, EventDto event);
    EventResponseDto deleteEvent(int ownerId, long eventId);
    EventResponseDto getEventById(int ownerId, long eventId);
    List<EventResponseDto> getEventByTitle(int ownerId, String title);
    List<EventResponseDto> getFutureEvents(int ownerId);
    List<EventResponseDto> getEventsByDate(int ownerId, LocalDate from, LocalDate to);
    List<EventResponseDto> getEventsBySentNetworks(int ownerId, List<String> networks);

}
