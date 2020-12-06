package gregad.eventmanager.usereventservice.rest;

import gregad.eventmanager.usereventservice.dto.EventDto;
import gregad.eventmanager.usereventservice.dto.EventResponseDto;
import gregad.eventmanager.usereventservice.services.event_service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static gregad.eventmanager.usereventservice.api.ApiConstants.EVENTS;
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
    
    @PatchMapping(value = "/{id}")
    EventResponseDto updateEvent(@PathVariable int ownerId, @RequestBody EventDto eventDto){
        return eventService.updateEvent(ownerId,eventDto);
    }
    
    @DeleteMapping
    EventResponseDto deleteEvent(@RequestParam int ownerId,@RequestParam long eventId){
        return eventService.deleteEvent(ownerId,eventId);
    }
    
    @GetMapping
    EventResponseDto getEventById(@RequestParam int ownerId,@RequestParam long eventId){
        return eventService.getEventById(ownerId,eventId);
    }
    
    @GetMapping(value = SEARCH+"/{id}")
    List<EventResponseDto> getFutureEvents(@PathVariable int ownerId){
        return eventService.getFutureEvents(ownerId);
    }

    @GetMapping(value = SEARCH+BY_TITLE)
    List<EventResponseDto> getEventsByTitle(@RequestParam int ownerId,@RequestParam String title){
        return eventService.getEventByTitle(ownerId,title);
    }

    @GetMapping(value = SEARCH+BY_GUEST)
    List<EventResponseDto> getEventsByNetworks(@RequestParam int ownerId,@RequestParam int guestId){
        return eventService.getEventsByInvitedUser(ownerId, guestId);
    }

    @GetMapping(value = SEARCH+BY_DATES)
    List<EventResponseDto> getEventsByNetworks(@RequestParam int id, @RequestParam LocalDate from, @RequestParam LocalDate to ){
        return eventService.getEventsByDate(id,from,to);
    }
}
