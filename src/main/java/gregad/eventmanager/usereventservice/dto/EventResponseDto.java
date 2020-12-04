package gregad.eventmanager.usereventservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import gregad.eventmanager.usereventservice.model.Message;
import gregad.eventmanager.usereventservice.model.User;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * @author Greg Adler
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDto {
    private long id;
    private User owner;
    private String title;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;
    @JsonFormat(pattern = "KK:mm a")
    private LocalTime eventTime;
    private String imageUrl;
    private String telegramChannelRef;
    private Map<String,List<User>>sentToNetworkConnections;
    private Map<String, List<User>> invited;
    private Map<String, List<Message>> correspondences;

}
