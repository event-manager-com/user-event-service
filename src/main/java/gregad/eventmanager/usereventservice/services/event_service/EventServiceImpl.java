package gregad.eventmanager.usereventservice.services.event_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gregad.event_manager.loggerstarter.aspect.DoLogging;
import gregad.eventmanager.usereventservice.dao.EventDao;
import gregad.eventmanager.usereventservice.dao.SequenceDao;
import gregad.eventmanager.usereventservice.dto.EventDto;
import gregad.eventmanager.usereventservice.dto.EventResponseDto;
import gregad.eventmanager.usereventservice.model.DatabaseSequence;
import gregad.eventmanager.usereventservice.model.EventEntity;
import gregad.eventmanager.usereventservice.model.User;
import gregad.eventmanager.usereventservice.services.token_service.TokenHolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gregad.eventmanager.usereventservice.api.ApiConstants.HEADER;
import static gregad.eventmanager.usereventservice.api.ExternalApiConstants.*;

/**
 * @author Greg Adler
 */
@Service
@DoLogging(cleanLogDaysAgo = 10)
public class EventServiceImpl implements EventService {
    @Value("${history.service.url}")
    private String historyServiceUrl;
    @Value("${security.service.url}")
    private String securityServiceUrl;
    
    private TokenHolderService tokenHolderService;
    private RestTemplate restTemplate;
    private SequenceDao sequenceRepo;
    private EventDao eventRepo;
    private ObjectMapper objectMapper;

    @Autowired
    public EventServiceImpl(RestTemplate restTemplate, SequenceDao sequenceRepo, 
                            EventDao eventRepo, ObjectMapper objectMapper,TokenHolderService tokenHolderService) {
        this.restTemplate = restTemplate;
        this.sequenceRepo = sequenceRepo;
        this.eventRepo = eventRepo;
        this.objectMapper = objectMapper;
        this.tokenHolderService=tokenHolderService;
    }


    @Override
    public EventResponseDto createEvent(EventDto event) {
        EventEntity eventEntity = new  EventEntity();
                eventEntity.setId(getEventNextId());
                eventEntity.setOwner(event.getOwner());
                eventEntity.setTitle(event.getTitle());
                eventEntity.setDescription(event.getDescription());
                eventEntity.setEventDate(event.getEventDate());
                eventEntity.setEventTime(event.getEventTime());
                eventEntity.setImageUrl(event.getImageUrl());
                eventEntity.setTelegramChannelRef(event.getTelegramChannelRef());
                eventEntity.setApprovedGuests(new ArrayList<>());
                eventEntity.setInvited(new ArrayList<>());
                eventEntity.setCorrespondences(new ArrayList<>());
        eventRepo.save(eventEntity);
        return toEventResponseDto(eventEntity);
    }
    

    private EventResponseDto toEventResponseDto(EventEntity eventEntity) {
        EventResponseDto res = new EventResponseDto();
                res.setId(eventEntity.getId());
                res.setOwner(eventEntity.getOwner());
                res.setTitle(eventEntity.getTitle());
                res.setDescription(eventEntity.getDescription());
                res.setEventDate(eventEntity.getEventDate());
                res.setEventTime(eventEntity.getEventTime());
                res.setImageUrl(eventEntity.getImageUrl());
                res.setTelegramChannelRef(eventEntity.getTelegramChannelRef());
                res.setInvited(eventEntity.getInvited());
                res.setApprovedGuests(eventEntity.getApprovedGuests());
                res.setCorrespondences(eventEntity.getCorrespondences());
     return res;
    }

    private long getEventNextId() {
        DatabaseSequence databaseSequence = sequenceRepo.findById(1).orElse(null);
        long id;
        if (databaseSequence==null){
            sequenceRepo.save(new DatabaseSequence(1, 1L));
            id=1;
        }else {
            id=databaseSequence.getSeq()+1;
            databaseSequence.setSeq(id);
        }
        return id;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public EventResponseDto updateEvent(int ownerId, EventDto event) {
        long id = event.getId();
        EventEntity eventEntity = eventRepo.findByIdAndOwnerId(id,ownerId).orElseThrow(()->{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Event id:"+id+" not found in user id:"+ownerId+" storage");
        });
        eventEntity.setTitle(event.getTitle());
        eventEntity.setDescription(event.getDescription());
        eventEntity.setImageUrl(event.getImageUrl());
        eventEntity.setEventDate(event.getEventDate());
        eventEntity.setEventTime(event.getEventTime());
        eventRepo.save(eventEntity);
        return toEventResponseDto(eventEntity);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public EventResponseDto deleteEvent(int ownerId, long eventId) {
        EventEntity eventEntity = eventRepo.findByIdAndOwnerId(eventId,ownerId).orElseThrow(()->{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Event id:"+eventId+" not found in user id:"+ownerId+" storage");
        });
        eventRepo.delete(eventEntity);
        return toEventResponseDto(eventEntity);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
    @Override
    public EventResponseDto getEventById(int ownerId, long eventId) {
        EventEntity eventEntity = eventRepo.findByIdAndOwnerId(eventId,ownerId).orElse(null);
        if (eventEntity==null){
             eventEntity =restGetEventById(ownerId,eventId);
        }
//        if (eventEntity==null){
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
//                    "Event id:"+eventId+" not found in user id:"+ownerId+" storage");
//        }
        return toEventResponseDto(eventEntity);
    }

    private EventEntity restGetEventById(int ownerId, long eventId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HEADER,tokenHolderService.getToken());
        ResponseEntity<EventEntity> response = restTemplate.exchange(historyServiceUrl + SEARCH + "?eventId=" + eventId + "&ownerId=" + ownerId,
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                EventEntity.class);
        return response.getBody();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public List<EventResponseDto> getEventByTitle(int ownerId, String title) {
        List<EventEntity>events=eventRepo.findAllByOwnerIdAndTitleContaining(ownerId,title).orElse(new ArrayList<>());
        EventEntity[] eventsFromHistory = restGetEventByTitle(ownerId,title);
//        if (events.isEmpty() &&(eventsFromHistory==null || eventsFromHistory.length==0)){
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
//                    "Events with title:"+title+" not found in user id:"+ownerId+" storage");
//        }
        assert eventsFromHistory != null;
        events.addAll(Arrays.asList(eventsFromHistory));
        return events.stream().map(this::toEventResponseDto).collect(Collectors.toList());
    }

    private EventEntity[] restGetEventByTitle(int ownerId, String title) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HEADER,tokenHolderService.getToken());
        ResponseEntity<EventEntity[]> response = restTemplate.exchange(historyServiceUrl + SEARCH + BY_TITLE + "?ownerId=" + ownerId + "&title=" + title,
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                EventEntity[].class);
        return response.getBody();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public List<EventResponseDto> getFutureEvents(int ownerId) {
        List<EventEntity>entities=eventRepo.findAllByOwnerId(ownerId).orElse(new ArrayList<>());
        return entities.stream().map(this::toEventResponseDto).collect(Collectors.toList());
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public List<EventResponseDto> getEventsByDate(int ownerId, LocalDate from, LocalDate to) {
        if (to.isBefore(from)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Date from can`t be greater than date to");
        }
        LocalDate now = LocalDate.now();
        if (now.isAfter(from) && now.isBefore(to)){
            EventEntity[]eventsFromHistory=getFromHistory(ownerId,from,now);
            List<EventEntity> allFromRepo = getFromRepo(ownerId,now,to);
            allFromRepo.addAll(Arrays.asList(eventsFromHistory));
//            if (allFromRepo.isEmpty() && eventsFromHistory.length==0){
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
//                        "No events not fount to user id:"+ownerId+" between dates("+from+")("+to+")");
//            }
            return allFromRepo.stream().map(this::toEventResponseDto).collect(Collectors.toList());
        }
        if (now.isAfter(to)){
            EventEntity[] fromHistory = getFromHistory(ownerId, from, to);
//            if ( fromHistory.length==0){
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
//                        "No events not fount to user id:"+ownerId+" between dates("+from+")("+to+")");
//            }
            return Stream.of(fromHistory).map(this::toEventResponseDto).collect(Collectors.toList());
        }
        List<EventEntity> fromRepo = getFromRepo(ownerId, from, to);
//        if (fromRepo.isEmpty()){
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
//                    "No events not fount to user id:"+ownerId+" between dates("+from+")("+to+")");
//        }
        return fromRepo.stream().map(this::toEventResponseDto).collect(Collectors.toList());
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public List<EventResponseDto> getEventsByGuestName(int ownerId, String userInvited) {
        EventEntity[] eventsFromHistory = restGetEventByGuest(ownerId, userInvited);
        List<EventEntity> eventsFromRepo = eventRepo.findAllByOwnerId(ownerId)
                .orElse(new ArrayList<>())
                .stream()
                .filter(e -> e.getApprovedGuests().stream().anyMatch(u -> u.getName().equals(userInvited)))
                .collect(Collectors.toList());
        eventsFromRepo.addAll(Arrays.asList(eventsFromHistory));
//        if (eventsFromRepo.isEmpty()){
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
//                    "No events not fount to user id:"+ownerId+" with guest id:"+userInvitedId);
//        }
        return eventsFromRepo.stream().map(this::toEventResponseDto).collect(Collectors.toList());
    }
    
    private EventEntity[] restGetEventByGuest(int ownerId, String guest) {
        String url=historyServiceUrl+ SEARCH+BY_GUEST+"?ownerId="+ownerId+"&guest="+guest;
        
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HEADER,tokenHolderService.getToken());
        ResponseEntity<EventEntity[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(httpHeaders), EventEntity[].class);
        return response.getBody();
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    

    private List<EventEntity> getFromRepo(int ownerId, LocalDate from, LocalDate to) {
        return eventRepo.findAllByOwnerId(ownerId).orElse(new ArrayList<>())
                .stream()
                .filter(e->(e.getEventDate().isAfter(from) || e.getEventDate().isEqual(from))
                        && (e.getEventDate().isBefore(to) || e.getEventDate().isEqual(to)))
                .collect(Collectors.toList());
    }

    private EventEntity[] getFromHistory(int ownerId, LocalDate from, LocalDate to) {
        String url=historyServiceUrl + SEARCH+ BY_DATES + "?ownerId=" + ownerId + "&from=" + from + "&to=" + to;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HEADER,tokenHolderService.getToken());
        ResponseEntity<EventEntity[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(httpHeaders), EventEntity[].class);
        return response.getBody();
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public List<User> addEventInvitedUser(long eventId, User user) {
        EventEntity eventEntity = eventRepo.findById(eventId).orElseThrow(() -> {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Event id:" + eventId + " not found");
        });
        if (!eventEntity.getInvited().contains(user)){
            eventEntity.getInvited().add(user);
        }
        eventRepo.save(eventEntity);
        return eventEntity.getInvited();
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public List<User> addEventNewApprovedGuest(long eventId, User user) {
        EventEntity eventEntity = eventRepo.findById(eventId).orElseThrow(() -> {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Event id:" + eventId + " not found");
        });
        List<User> approvedGuests = eventEntity.getApprovedGuests();
        if (!approvedGuests.contains(user)){
            approvedGuests.add(user);
        }
        eventRepo.save(eventEntity);
        return approvedGuests;
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public List<User> removeEventNewApprovedGuest(long eventId, int guestId) {
        EventEntity eventEntity = eventRepo.findById(eventId).orElseThrow(() -> {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Event id:" + eventId + " not found");
        });
        List<User> approvedGuests = eventEntity.getApprovedGuests();
        approvedGuests.removeIf(u->u.getId()==guestId);
        eventRepo.save(eventEntity);
        return approvedGuests;
    }
}
