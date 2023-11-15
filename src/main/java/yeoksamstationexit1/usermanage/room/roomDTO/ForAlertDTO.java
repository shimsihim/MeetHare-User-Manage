package yeoksamstationexit1.usermanage.room.roomDTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ForAlertDTO {

    private LocalDate reserveTime;
    private String roomCode;
    private List<String> reserveMembers;

}
