package gregad.eventmanager.usereventservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Greg Adler
 */
@Setter
@Getter
@NoArgsConstructor
public class UserOwnerDto {
    private long id;
    private String name;
    private List<String> allowedSocialNetworks;
}
