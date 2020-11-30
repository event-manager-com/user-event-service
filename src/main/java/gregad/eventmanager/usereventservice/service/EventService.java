package gregad.eventmanager.usereventservice.service;

import gregad.eventmanager.usereventservice.dto.EventDto;
import gregad.eventmanager.usereventservice.dto.EventResponseDto;
import gregad.eventmanager.usereventservice.dto.SocialNetworkConnectionsDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * @author Greg Adler
 */
public interface EventService {
    List<SocialNetworkConnectionsDto> getAllConnections(long id);
    EventResponseDto createEvent(EventDto event);
    EventResponseDto updateEvent(long ownerId, EventDto event);
    EventResponseDto deleteEvent(long ownerId, long eventId);
    EventResponseDto getEventById(long ownerId, long eventId);
    List<EventResponseDto> getEventByTitle(long ownerId, String title);
    List<EventResponseDto> getFutureEvents(long ownerId);
    List<EventResponseDto> getEventsByDate(long ownerId, LocalDate from, LocalDate to);
    List<EventResponseDto> getEventsBySentNetworks(long ownerId, List<String> networks);

}
