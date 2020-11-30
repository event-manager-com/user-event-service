package gregad.eventmanager.usereventservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Greg Adler
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private User user;
    private String text;
}
