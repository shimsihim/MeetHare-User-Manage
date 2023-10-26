package yeoksamstationexit1.usermanage.room.participant.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class ChangeLocalStartRequestDTO {

    Long roomId;

    String startPoint;
}
