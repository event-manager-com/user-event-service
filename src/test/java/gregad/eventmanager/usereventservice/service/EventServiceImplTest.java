package gregad.eventmanager.usereventservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gregad.eventmanager.usereventservice.UserEventServiceApplication;
import gregad.eventmanager.usereventservice.dao.EventDao;
import gregad.eventmanager.usereventservice.dao.SequenceDao;
import gregad.eventmanager.usereventservice.dto.EventDto;
import gregad.eventmanager.usereventservice.dto.SocialNetworkConnectionsDto;
import gregad.eventmanager.usereventservice.model.User;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * @author Greg Adler
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = UserEventServiceApplication.class)
class EventServiceImplTest {
    EventDto eventDto = new EventDto(0, new User(1, "Greg"),
            "Some event", "no description",
            LocalDate.of(2020, 12, 31),
            LocalTime.of(23, 59),
            "http://someImage",
            "http://someRef",
            Map.of("facebook", List.of(new User(2, "Mo"), new User(3, "Ho"))));
    private EventService eventService;
    private RestTemplate restTemplate;
    private SequenceDao sequenceRepo;
    private EventDao eventRepo;
    private ObjectMapper objectMapper=new ObjectMapper();

    @BeforeEach
    public void init(){
        restTemplate=Mockito.mock(RestTemplate.class);
        sequenceRepo=Mockito.mock(SequenceDao.class);
        eventRepo=Mockito.mock(EventDao.class);
        eventService=new EventServiceImpl(restTemplate,sequenceRepo,eventRepo,objectMapper);
    }

    @SneakyThrows
    @Test
    void getAllConnections() {
//        String[]userNetworks={"facebook","twitter","instagram"};
//        SocialNetworkConnectionsDto facebook = getConnections("facebook");
//        SocialNetworkConnectionsDto twitter = getConnections("twitter");
//        SocialNetworkConnectionsDto instagram = getConnections("instagram");
//
//        SocialNetworkConnectionsDto[]connections={facebook,twitter,instagram};
//
//        
//        when(restTemplate.getForObject(new URI("http://user-service/users"+"/networks" + "/" + 1), String[].class))
//                .thenReturn(userNetworks);
//        when(restTemplate.getForObject(new URI(getRouterUrl(1, userNetworks)), SocialNetworkConnectionsDto[].class))
//                .thenReturn(connections);
//
//        List<SocialNetworkConnectionsDto> allConnections = eventService.getAllConnections("1");
//        Assert.assertEquals(9,allConnections.size());
//
//        try {
//            eventService.getAllConnections(2);
//        } catch (ResponseStatusException e) {
//            Assert.assertEquals("No Social networks not found to user id:2",e.getReason());
//        }
    }

    private SocialNetworkConnectionsDto getConnections(String network) {
        return new SocialNetworkConnectionsDto(network, Arrays.asList(
                new User(11, "Gor"+network),
                new User(22,"Irvin"+network),
                new User(33,"Ave"+network)));
    }

    private String getRouterUrl(long id, String[] userNetworks) {
        String res="http://router-service/router/users/connections"+"?id="+id+"&networks="+userNetworks[0];
        for (int i = 0; i < userNetworks.length; i++) {
            res=res+","+userNetworks[i];
        }
        return res;
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    void createEvent() {
        
    }

    @Test
    void updateEvent() {
    }

    @Test
    void deleteEvent() {
    }

    @Test
    void getEventById() {
    }

    @Test
    void getEventByTitle() {
    }

    @Test
    void getFutureEvents() {
    }

    @Test
    void getEventsByDate() {
    }

    @Test
    void getEventsBySentNetworks() {
    }
}