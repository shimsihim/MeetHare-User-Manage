package yeoksamstationexit1.usermanage.route.dto.request.station;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class StartPointListDTO {

    private String nickName;

    private String stationName;

    private double latitude;

    private double longitude;


}
