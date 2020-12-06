package gregad.eventmanager.usereventservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import gregad.eventmanager.usereventservice.UserEventServiceApplication;
import gregad.eventmanager.usereventservice.dao.EventDao;
import gregad.eventmanager.usereventservice.dao.SequenceDao;
import gregad.eventmanager.usereventservice.dto.EventDto;
import gregad.eventmanager.usereventservice.model.User;
import gregad.eventmanager.usereventservice.services.event_service.EventService;
import gregad.eventmanager.usereventservice.services.event_service.EventServiceImpl;
import gregad.eventmanager.usereventservice.services.token_service.TokenHolderService;
import gregad.eventmanager.usereventservice.services.token_service.TokenHolderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;

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
            "http://someRef");
    private EventService eventService;
    private RestTemplate restTemplate;
    private SequenceDao sequenceRepo;
    private EventDao eventRepo;
    private ObjectMapper objectMapper=new ObjectMapper();
    private TokenHolderService tokenHolderService;

    @BeforeEach
    public void init(){
        restTemplate=Mockito.mock(RestTemplate.class);
        sequenceRepo=Mockito.mock(SequenceDao.class);
        eventRepo=Mockito.mock(EventDao.class);
        eventService=new EventServiceImpl(restTemplate,sequenceRepo,eventRepo,objectMapper,
                new TokenHolderServiceImpl(objectMapper,restTemplate));
    }

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