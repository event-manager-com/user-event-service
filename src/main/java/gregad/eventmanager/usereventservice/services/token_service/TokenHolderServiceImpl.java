package gregad.eventmanager.usereventservice.services.token_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gregad.eventmanager.usereventservice.dto.NamePassword;
import gregad.eventmanager.usereventservice.dto.Token;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

/**
 * @author Greg Adler
 */
@Component
public class TokenHolderServiceImpl implements TokenHolderService {
    private ObjectMapper objectMapper;
    private RestTemplate restTemplate;
    @Value("${security.service.url}")
    private String securityServiceUrl;

    private String token;
    @Value("${security.user.name}")
    private String secUserName;
    @Value("${security.user.password}")
    private String secPassword;
    
    private NamePassword namePassword;

    @Autowired
    public TokenHolderServiceImpl(ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }
    
    @PostConstruct
    void initNamePassword(){
        namePassword=new NamePassword(secUserName,secPassword);
    }

    @Override
    @SneakyThrows
    @Scheduled(cron = "0 15 0 * * *")
    public void refreshToken() {
        String jsonNamePassword = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(namePassword);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonNamePassword, httpHeaders);
        token = restTemplate.postForObject(securityServiceUrl + "/generate", request, Token.class).getToken();
    }

    @Override
    public String getToken() {
        return token;
    }
}
