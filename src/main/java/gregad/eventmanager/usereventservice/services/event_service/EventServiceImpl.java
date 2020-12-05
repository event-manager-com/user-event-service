package gregad.eventmanager.usereventservice.services.event_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gregad.eventmanager.usereventservice.dao.EventDao;
import gregad.eventmanager.usereventservice.dao.SequenceDao;
import gregad.eventmanager.usereventservice.dto.*;
import gregad.eventmanager.usereventservice.model.DatabaseSequence;
import gregad.eventmanager.usereventservice.model.EventEntity;
import gregad.eventmanager.usereventservice.model.User;
import gregad.eventmanager.usereventservice.services.token_service.TokenHolderService;

import static gregad.eventmanager.usereventservice.api.ApiConstants.*;
import static gregad.eventmanager.usereventservice.api.ExternalApiConstants.*;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gregad.eventmanager.usereventservice.api.ExternalApiConstants.*;

/**
 * @author Greg Adler
 */
@Service
public class EventServiceImpl implements EventService {
    @Value("${router.service.url}")
    private String routerUrl;
    @Value("${user.service.url}")
    private String userServiceUrl;
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
    public List<SocialNetworkConnectionsDto> getAllConnections(int id) {      
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER,tokenHolderService.getToken());
        String[] userNetworks = getUserNetworks(id,headers);
        if (userNetworks==null || userNetworks.length==0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"No Social networks not found to user id:"+id);
        }
        SocialNetworkConnectionsDto[] networkConnections =getUserNetworksConnections(id,userNetworks,headers);
        return Arrays.asList(networkConnections);
    }

    private SocialNetworkConnectionsDto[] getUserNetworksConnections(int id, String[] userNetworks, HttpHeaders headers) {
        ResponseEntity<SocialNetworkConnectionsDto[]> networkConnections =
                restTemplate.exchange(getRouterUrl(id, userNetworks),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        SocialNetworkConnectionsDto[].class);
        return networkConnections.getBody();
    }

    private String[] getUserNetworks(int id, HttpHeaders headers) {
        ResponseEntity<String[]> userNetworksResponseEntity =
                restTemplate.exchange(userServiceUrl+"/networks" + "/" + id,
                        HttpMethod.GET,new HttpEntity<>(headers), String[].class);
        return userNetworksResponseEntity.getBody();
    }

    private String getRouterUrl(int id, String[] userNetworks) {
        String res=routerUrl+USER+CONNECTIONS+"?id="+id+"&networks="+userNetworks[0];
        for (int i = 1; i < userNetworks.length; i++) {
            res=res+","+userNetworks[i];
        }
        return res;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public EventResponseDto createEvent(EventDto event) {
        Map<String, List<User>> sendToNetworks = event.getSendToNetworks();
        EventEntity eventEntity = new  EventEntity();
                eventEntity.setId(getEventNextId());
                eventEntity.setOwner(event.getOwner());
                eventEntity.setTitle(event.getTitle());
                eventEntity.setDescription(event.getDescription());
                eventEntity.setEventDate(event.getEventDate());
                eventEntity.setEventTime(event.getEventTime());
                eventEntity.setImageUrl(event.getImageUrl());
                eventEntity.setTelegramChannelRef(event.getTelegramChannelRef());
                eventEntity.setSentToNetworkConnections(sendToNetworks);
                eventEntity.setInvited(new HashMap<>());
                eventEntity.setCorrespondences(new HashMap<>());
               
        if (sendToNetworks.size()==0 ||
                sendToNetworks.values().stream().mapToLong(List::size).sum() ==0){
            eventRepo.save(eventEntity);
            return toEventResponseDto(eventEntity);
        }
        sendEvent(eventEntity);
        eventRepo.save(eventEntity);
        return toEventResponseDto(eventEntity);
    }

    @SneakyThrows
    private void sendEvent(EventEntity eventEntity) {
      
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HEADER,tokenHolderService.getToken());
        EventDto eventDto = new EventDto();
                eventDto.setId(eventEntity.getId());
                eventDto.setOwner(eventEntity.getOwner());
                eventDto.setTitle(eventEntity.getTitle());
                eventDto.setDescription(eventEntity.getDescription());
                eventDto.setEventDate(eventEntity.getEventDate());
                eventDto.setEventTime(eventEntity.getEventTime());
                eventDto.setImageUrl(eventEntity.getImageUrl());
                eventDto.setTelegramChannelRef(eventEntity.getTelegramChannelRef());
                eventDto.setSendToNetworks(eventEntity.getSentToNetworkConnections());
        String eventJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(eventDto);
        HttpEntity<String> request = new HttpEntity<>(eventJson, headers);
        Boolean isAdded = restTemplate.postForObject(routerUrl+USER+EVENTS, request, Boolean.class);
        if (isAdded==null ||!isAdded){
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"");//TODO fill message
        }
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
                res.setSentToNetworkConnections(eventEntity.getSentToNetworkConnections());
                res.setInvited(eventEntity.getInvited());
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
        if (!eventEntity.getEventDate().equals(event.getEventDate() )||
                !eventEntity.getEventTime().equals(event.getEventTime())){
            eventEntity.setEventDate(event.getEventDate());
            eventEntity.setEventTime(event.getEventTime());
            sendEvent(eventEntity);
        }
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
        if (eventEntity==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Event id:"+eventId+" not found in user id:"+ownerId+" storage");
        }
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
        if (events.isEmpty() &&(eventsFromHistory==null || eventsFromHistory.length==0)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Events with title:"+title+" not found in user id:"+ownerId+" storage");
        }
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
        List<EventEntity>entities=eventRepo.findAllByOwnerId(ownerId).orElseThrow(()->{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No future events not found to user id:"+ownerId);
        });
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
            if (allFromRepo.isEmpty() && eventsFromHistory.length==0){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No events not fount to user id:"+ownerId+" between dates("+from+")("+to+")");
            }
            return allFromRepo.stream().map(this::toEventResponseDto).collect(Collectors.toList());
        }
        if (now.isAfter(to)){
            EventEntity[] fromHistory = getFromHistory(ownerId, from, to);
            if ( fromHistory.length==0){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No events not fount to user id:"+ownerId+" between dates("+from+")("+to+")");
            }
            return Stream.of(fromHistory).map(this::toEventResponseDto).collect(Collectors.toList());
        }
        List<EventEntity> fromRepo = getFromRepo(ownerId, from, to);
        if (fromRepo.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No events not fount to user id:"+ownerId+" between dates("+from+")("+to+")");
        }
        return fromRepo.stream().map(this::toEventResponseDto).collect(Collectors.toList());
    }

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
    public List<EventResponseDto> getEventsBySentNetworks(int ownerId, List<String> networks) {
        if (networks.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Have not specified social networks");
        }
        Set<EventEntity> events= eventRepo.findAllByOwnerId(ownerId).orElse(new ArrayList<>()).stream()
                .filter(e-> e.getSentToNetworkConnections().keySet().stream().anyMatch(networks::contains))
                .collect(Collectors.toSet());
      
        EventEntity[] eventsFromHistory = restGetEventBySentNetworks(ownerId,networks);
        if (events.isEmpty() &&(eventsFromHistory==null || eventsFromHistory.length==0)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No events not fount to user id:"+ownerId+" with the specified networks");
        }
        assert eventsFromHistory != null;
        events.addAll(Arrays.asList(eventsFromHistory));
        return events.stream().map(this::toEventResponseDto).collect(Collectors.toList());
    }

    private EventEntity[] restGetEventBySentNetworks(int ownerId, List<String> networks) {
        String url=historyServiceUrl+ SEARCH+BY_NETWORKS+"?ownerId="+ownerId+"&networks="+networks.get(0);
        for (int i = 1; i < networks.size(); i++) {
            url=url+","+networks.get(i);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HEADER,tokenHolderService.getToken());
        ResponseEntity<EventEntity[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(httpHeaders), EventEntity[].class);
        return response.getBody();
    }
}
