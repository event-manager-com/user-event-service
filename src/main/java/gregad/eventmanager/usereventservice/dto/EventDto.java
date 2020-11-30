package gregad.eventmanager.usereventservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import gregad.eventmanager.usereventservice.model.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@Builder
public class EventDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private long id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean toDelete;
    private User owner;
    private String title;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;
    @JsonFormat(pattern = "KK:mm a")
    private LocalTime eventTime;
    private Map<String,List<User>>sendToNetworks;

}
