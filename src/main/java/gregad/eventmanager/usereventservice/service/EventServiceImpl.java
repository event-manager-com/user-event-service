package gregad.eventmanager.usereventservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gregad.eventmanager.usereventservice.dao.EventDao;
import gregad.eventmanager.usereventservice.dao.SequenceDao;
import gregad.eventmanager.usereventservice.dto.EventDto;
import gregad.eventmanager.usereventservice.dto.EventResponseDto;
import gregad.eventmanager.usereventservice.dto.SocialNetworkConnectionsDto;
import gregad.eventmanager.usereventservice.model.DatabaseSequence;
import gregad.eventmanager.usereventservice.model.EventEntity;
import gregad.eventmanager.usereventservice.model.User;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gregad.eventmanager.usereventservice.api.RouterApiConstants.*;

/**
 * @author Greg Adler
 */
@Service
public class EventServiceImpl implements EventService {
    @Value("${router.service}")
    private String routerUrl;
    @Value("${user.service}")
    private String userServiceUrl;
    
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private SequenceDao sequenceRepo;
    @Autowired
    private EventDao eventRepo;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<SocialNetworkConnectionsDto> getAllConnections(long id) {
        String[] userNetworks = restTemplate.getForObject(userServiceUrl+"/networks" + "/" + id, String[].class);
        if (userNetworks==null || userNetworks.length==0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"No Social networks not found to user id:"+id);
        }
        SocialNetworkConnectionsDto[] networkConnections = restTemplate.getForObject(getRouterUrl(id, userNetworks), SocialNetworkConnectionsDto[].class);
        assert networkConnections != null;
        return Arrays.asList(networkConnections);
    }

    private String getRouterUrl(long id, String[] userNetworks) {
        String res=routerUrl+USER+CONNECTIONS+"?id="+id+"&networks="+userNetworks[0];
        for (int i = 0; i < userNetworks.length; i++) {
            res=res+","+userNetworks[i];
        }
        return res;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public EventResponseDto createEvent(EventDto event) {
        Map<String, List<User>> sendToNetworks = event.getSendToNetworks();
        EventEntity eventEntity = EventEntity.builder()
                .id(getEventNextId())
                .owner(event.getOwner())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .eventTime(event.getEventTime())
                .sentToNetworkConnections(sendToNetworks)
                .invited(new HashMap<>())
                .correspondences(new HashMap<>())
                .build();
        if (sendToNetworks.size()==0 ||
                sendToNetworks.values().stream().mapToLong(List::size).sum() ==0){
            eventRepo.save(eventEntity);
            return toEventResponseDto(eventEntity);
        }
        sendEvent(false,eventEntity);
        eventRepo.save(eventEntity);
        return toEventResponseDto(eventEntity);
    }

    @SneakyThrows
    private void sendEvent(boolean toDelete,EventEntity eventEntity) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        EventDto eventDto = EventDto.builder()
                .id(eventEntity.getId())
                .toDelete(toDelete)
                .owner(eventEntity.getOwner())
                .title(eventEntity.getTitle())
                .description(eventEntity.getDescription())
                .eventDate(eventEntity.getEventDate())
                .eventTime(eventEntity.getEventTime())
                .sendToNetworks(eventEntity.getSentToNetworkConnections())
                .build();
        String eventJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(eventDto);
        if (!toDelete) {
            HttpEntity<String> request = new HttpEntity<String>(eventJson, headers);
            Boolean isAdded = restTemplate.postForObject(routerUrl+USER+EVENTS, request, Boolean.class);
            if (!isAdded){
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"");//TODO fill message
            }
        }
    }

    private EventResponseDto toEventResponseDto(EventEntity eventEntity) {
        return EventResponseDto.builder()
                .id(eventEntity.getId())
                .owner(eventEntity.getOwner())
                .title(eventEntity.getTitle())
                .description(eventEntity.getDescription())
                .eventDate(eventEntity.getEventDate())
                .eventTime(eventEntity.getEventTime())
                .sentToNetworkConnections(eventEntity.getSentToNetworkConnections())
                .invited(eventEntity.getInvited())
                .correspondences(eventEntity.getCorrespondences())
                .build();
    }

    private long getEventNextId() {
        DatabaseSequence databaseSequence = sequenceRepo.findById(1).orElse(null);
        long id;
        if (databaseSequence==null){
            sequenceRepo.save(new DatabaseSequence(1,1l));
            id=1;
        }else {
            id=databaseSequence.getSeq()+1;
            databaseSequence.setSeq(id);
        }
        return id;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public EventResponseDto updateEvent(long ownerId, EventDto event) {
        long id = event.getId();
        EventEntity eventEntity = eventRepo.findById(id).orElseThrow(()->{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Event id:"+id+" not found in user id:"+ownerId+" storage");
        });
        checkIfOwner(ownerId,eventEntity);
        eventEntity.setTitle(event.getTitle());
        eventEntity.setDescription(event.getDescription());
        if (!eventEntity.getEventDate().equals(event.getEventDate() )||
                !eventEntity.getEventTime().equals(event.getEventTime())){
            eventEntity.setEventDate(event.getEventDate());
            eventEntity.setEventTime(event.getEventTime());
            sendEvent(false,eventEntity);
        }
        eventRepo.save(eventEntity);
        return toEventResponseDto(eventEntity);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public EventResponseDto deleteEvent(long ownerId, long eventId) {
        EventEntity eventEntity = eventRepo.findById(eventId).orElseThrow(()->{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Event id:"+eventId+" not found in user id:"+ownerId+" storage");
        });
        checkIfOwner(ownerId,eventEntity);
        sendEvent(true,eventEntity);
        eventRepo.delete(eventEntity);
        return toEventResponseDto(eventEntity);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
    @Override
    public EventResponseDto getEventById(long ownerId, long eventId) {
        EventEntity eventEntity = eventRepo.findById(eventId).orElse(null);
        if (eventEntity==null){
             eventEntity = restTemplate.getForObject(routerUrl+EVENTS+HISTORY+"/"+eventId, EventEntity.class);
        }
        assert eventEntity != null;
        checkIfOwner(ownerId,eventEntity);
        return toEventResponseDto(eventEntity);
    }

    private void checkIfOwner(long ownerId, EventEntity eventEntity) {
        if (ownerId!=eventEntity.getOwner().getId()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Event id:"+eventEntity.getId()+" not found in user id:"+ownerId+" storage");
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public List<EventResponseDto> getEventByTitle(long ownerId, String title) {
        List<EventEntity>events=eventRepo.findAllByOwnerIdAndTitleContaining(ownerId,title);
        EventEntity[] eventsFromHistory = 
                restTemplate.getForObject(routerUrl + USER + EVENTS + HISTORY + BY_TITLE + "?ownerId=" + ownerId + "&title=" + title, EventEntity[].class);
        assert eventsFromHistory != null;
        events.addAll(Arrays.asList(eventsFromHistory));
        return events.stream().map(this::toEventResponseDto).collect(Collectors.toList());
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public List<EventResponseDto> getFutureEvents(long ownerId) {
        List<EventEntity>entities=eventRepo.findAllByOwnerId(ownerId);
        return entities.stream().map(this::toEventResponseDto).collect(Collectors.toList());
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public List<EventResponseDto> getEventsByDate(long ownerId, LocalDate from, LocalDate to) {
        LocalDate now = LocalDate.now();
        if (now.isAfter(from) && now.isBefore(to)){
            EventEntity[]eventsFromHistory=getFromHistory(ownerId,from,now);
            List<EventEntity> allFromRepo = getFromRepo(ownerId,now,to);
            allFromRepo.addAll(Arrays.asList(eventsFromHistory));
            return allFromRepo.stream().map(this::toEventResponseDto).collect(Collectors.toList());
        }
        if (now.isAfter(to)){
            EventEntity[] fromHistory = getFromHistory(ownerId, from, to);
            return Stream.of(fromHistory).map(this::toEventResponseDto).collect(Collectors.toList());
        }
        return getFromRepo(ownerId,from,to).stream().map(this::toEventResponseDto).collect(Collectors.toList());
    }

    private List<EventEntity> getFromRepo(long ownerId, LocalDate from, LocalDate to) {
        return eventRepo.findAllByOwnerId(ownerId).stream()
                .filter(e->e.getEventDate().isAfter(from) && e.getEventDate().isBefore(to)).collect(Collectors.toList());
    }

    private EventEntity[] getFromHistory(long ownerId, LocalDate from, LocalDate to) {
        String url=routerUrl + USER + EVENTS + HISTORY + BY_DATES + "?ownerId=" + ownerId + "&from=" + from + "&to=" + to;
        EventEntity[] events = restTemplate.getForObject(url, EventEntity[].class);
        return events;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public List<EventResponseDto> getEventsBySentNetworks(long ownerId, List<String> networks) {
        if (networks.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Have not specified social networks");
        }
        Set<EventEntity> events= eventRepo.findAllByOwnerId(ownerId).stream()
                .filter(e->e.getSentToNetworkConnections().keySet().stream().filter(n->networks.contains(n)).count()>0)
                .collect(Collectors.toSet());
        String url=routerUrl+ USER + EVENTS + HISTORY+BY_NETWORKS+"?ownerId="+ownerId+"&networks="+networks.get(0);
        for (int i = 1; i < networks.size(); i++) {
             url=url+","+networks.get(i);
        }
        EventEntity[] eventsFromHistory = restTemplate.getForObject(url, EventEntity[].class);
        assert eventsFromHistory != null;
        events.addAll(Arrays.asList(eventsFromHistory));
        return events.stream().map(this::toEventResponseDto).collect(Collectors.toList());
    }
}
