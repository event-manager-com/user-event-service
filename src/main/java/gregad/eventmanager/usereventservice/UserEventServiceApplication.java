package gregad.eventmanager.usereventservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class UserEventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserEventServiceApplication.class, args);
    }

}
