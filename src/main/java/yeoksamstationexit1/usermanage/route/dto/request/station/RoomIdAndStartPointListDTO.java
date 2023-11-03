package yeoksamstationexit1.usermanage.route.dto.request.station;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class RoomIdAndStartPointListDTO {

    private long roomId;

    private List<StartPointListDTO> startPointList;


}
