package gregad.eventmanager.usereventservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UserEventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserEventServiceApplication.class, args);
    }

}
