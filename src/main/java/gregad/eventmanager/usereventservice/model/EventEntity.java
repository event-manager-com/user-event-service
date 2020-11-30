package gregad.eventmanager.usereventservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * @author Greg Adler
 */
@Getter
@Setter
@NoArgsConstructor
@Builder
@Document(collection = "events")
public class EventEntity {
    @Id
    private long id;
    private User owner;
    private String title;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;
    @JsonFormat(pattern = "KK:mm a")
    private LocalTime eventTime;
    private Map<String,List<User>>sentToNetworkConnections;
    private Map<String, List<User>> invited;
    private Map<String, List<Message>> correspondences;

}
