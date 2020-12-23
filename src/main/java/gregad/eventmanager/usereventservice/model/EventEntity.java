package gregad.eventmanager.usereventservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * @author Greg Adler
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = " future-event")
public class EventEntity {
    @Id
    private long id;
    @Indexed
    private User owner;
    private String title;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;
    @JsonFormat(pattern = "KK:mm a")
    private LocalTime eventTime;
    private String imageUrl;
    private String telegramChannelRef;
    private List<User> invited;
    private List<User> approvedGuests;
    private List<Message> correspondences;

}
