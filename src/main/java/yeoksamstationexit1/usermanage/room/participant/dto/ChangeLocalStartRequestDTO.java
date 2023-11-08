package yeoksamstationexit1.usermanage.room.participant.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class ChangeLocalStartRequestDTO {

    private Long roomId;

    private String startPoint;

    private double latitude;

    private double longitude;
}
