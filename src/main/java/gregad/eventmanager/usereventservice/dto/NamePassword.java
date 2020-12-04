package gregad.eventmanager.usereventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Greg Adler
 */
@Data
@AllArgsConstructor
public class NamePassword {
    private String name;
    private String password;
}
