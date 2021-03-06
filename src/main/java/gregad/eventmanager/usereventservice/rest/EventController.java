package gregad.eventmanager.usereventservice.rest;

import gregad.eventmanager.usereventservice.dto.EventDto;
import gregad.eventmanager.usereventservice.dto.EventResponseDto;
import gregad.eventmanager.usereventservice.model.User;
import gregad.eventmanager.usereventservice.services.event_service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static gregad.eventmanager.usereventservice.api.ApiConstants.EVENTS;
import static gregad.eventmanager.usereventservice.api.ApiConstants.INVITED;
import static gregad.eventmanager.usereventservice.api.ExternalApiConstants.*;

/*
 * @author Greg Adler
 */
@RestController
@RequestMapping(EVENTS)
public class EventController {
    @Autowired
    private EventService eventService;
    
    
    @PostMapping
    EventResponseDto createEvent(@RequestBody EventDto event){
        return eventService.createEvent(event);
    }
    
    @PatchMapping(value = "/{ownerId}")
    EventResponseDto updateEvent(@PathVariable int ownerId, @RequestBody EventDto eventDto){
        return eventService.updateEvent(ownerId,eventDto);
    }
    
    @DeleteMapping
    EventResponseDto deleteEvent(@RequestParam int ownerId,@RequestParam long eventId){
        return eventService.deleteEvent(ownerId,eventId);
    }
    
    @PostMapping(value = INVITED+"/{eventId}")
    public List<User> addInvitedUserToEvent(@PathVariable long eventId,
                                      @RequestBody User user){
        return eventService.addEventInvitedUser(eventId, user);
    }

    @PostMapping(value = BY_GUEST+"/{eventId}")
    public List<User> addGuestToEvent(@PathVariable long eventId,
                                      @RequestBody User user){
        return eventService.addEventNewApprovedGuest(eventId, user);
    }

    @DeleteMapping(value = BY_GUEST)
    public List<User> addGuestToEvent(@RequestParam long eventId,
                                      @RequestParam int guestId){
        return eventService.removeEventNewApprovedGuest(eventId, guestId);
    }
    
    @GetMapping
    EventResponseDto getEventById(@RequestParam int ownerId,@RequestParam long eventId){
        return eventService.getEventById(ownerId,eventId);
    }
    
    @GetMapping(value = SEARCH+"/{ownerId}")
    List<EventResponseDto> getFutureEvents(@PathVariable int ownerId){
        return eventService.getFutureEvents(ownerId);
    }

    @GetMapping(value = SEARCH+BY_TITLE)
    List<EventResponseDto> getEventsByTitle(@RequestParam int ownerId,@RequestParam String title){
        return eventService.getEventByTitle(ownerId,title);
    }

    @GetMapping(value = SEARCH+BY_GUEST)
    List<EventResponseDto> getEventsByGuest(@RequestParam int ownerId,@RequestParam String guest){
        return eventService.getEventsByGuestName(ownerId, guest);
    }

    @GetMapping(value = SEARCH+BY_DATES)
    List<EventResponseDto> getEventsByDates(@RequestParam int ownerId,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to ){
        return eventService.getEventsByDate(ownerId,from,to);
    }
}
