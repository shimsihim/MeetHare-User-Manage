package yeoksamstationexit1.usermanage.route.dto.request.place;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FixStationAndRoomIdDTO {

    private long roomId;


    private FixStationDTO fixStation;
}
