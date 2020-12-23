package gregad.eventmanager.usereventservice.services.event_service;

import gregad.eventmanager.usereventservice.dto.EventDto;
import gregad.eventmanager.usereventservice.dto.EventResponseDto;
import gregad.eventmanager.usereventservice.model.User;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Greg Adler
 */
public interface EventService {
    EventResponseDto createEvent(EventDto event);
    EventResponseDto updateEvent(int ownerId, EventDto event);
    EventResponseDto deleteEvent(int ownerId, long eventId);
    EventResponseDto getEventById(int ownerId, long eventId);
    List<EventResponseDto> getEventByTitle(int ownerId, String title);
    List<EventResponseDto> getFutureEvents(int ownerId);
    List<EventResponseDto> getEventsByDate(int ownerId, LocalDate from, LocalDate to);
    List<EventResponseDto> getEventsByGuestName(int ownerId, String userInvited);
    List<User> addEventInvitedUser(long eventId, User user);
    List<User> addEventNewApprovedGuest(long eventId, User user);
}
