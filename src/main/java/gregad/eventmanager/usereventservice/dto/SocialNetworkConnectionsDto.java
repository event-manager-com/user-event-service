package gregad.eventmanager.usereventservice.dto;

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
public class SocialNetworkConnectionsDto {
    private String network;
    private List<SocialNetworkUserDto>connections;
}
