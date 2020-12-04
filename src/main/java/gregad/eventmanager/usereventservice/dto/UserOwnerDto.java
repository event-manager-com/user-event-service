package gregad.eventmanager.usereventservice.dto;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class UserOwnerDto {
    private int id;
    private String name;
    private List<String> allowedSocialNetworks;
}
