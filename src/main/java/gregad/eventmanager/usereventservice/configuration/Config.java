package gregad.eventmanager.usereventservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * @author Greg Adler
 */
@Configuration
public class Config {
    
        @Bean
        @LoadBalanced
        public RestTemplate restTemplate(RestTemplateBuilder builder) {
            return builder
                    .setConnectTimeout(Duration.ofMillis(5000))
                    .setReadTimeout(Duration.ofMillis(5000))
                    .build();
        }
        
        @Bean
         public ObjectMapper objectMapper(){
            return new ObjectMapper();
        }
}
