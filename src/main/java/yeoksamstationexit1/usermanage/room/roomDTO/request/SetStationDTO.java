package yeoksamstationexit1.usermanage.room.roomDTO.request;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SetStationDTO {
    private long roomId;
    private int station;
}
