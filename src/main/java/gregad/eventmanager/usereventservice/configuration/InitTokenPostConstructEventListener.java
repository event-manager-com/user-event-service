package gregad.eventmanager.usereventservice.configuration;

import gregad.eventmanager.usereventservice.services.event_service.EventServiceImpl;
import gregad.eventmanager.usereventservice.services.token_service.TokenHolderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author Greg Adler
 */
@Component
public class InitTokenPostConstructEventListener implements ApplicationListener<ContextRefreshedEvent> {
    public static boolean isExecuted;
    @Autowired
    private ApplicationContext context;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (!isExecuted) {
            isExecuted=true;
            TokenHolderServiceImpl bean = context.getBean(TokenHolderServiceImpl.class);
            bean.refreshToken();
        }
    }
}
