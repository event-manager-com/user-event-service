package gregad.eventmanager.usereventservice.dto;

import gregad.eventmanager.usereventservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Greg Adler
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SocialNetworkConnectionsDto {
    private String network;
    private List<User>connections;
}
