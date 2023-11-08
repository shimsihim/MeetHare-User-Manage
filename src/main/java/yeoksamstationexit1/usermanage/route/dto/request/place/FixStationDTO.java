package yeoksamstationexit1.usermanage.route.dto.request.place;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class FixStationDTO {

    private int station_id;

    private String category;

    private List<Long> user_list;

}
