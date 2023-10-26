package yeoksamstationexit1.usermanage.room.roomDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceRequestDTO {

    private int station_id;
    private String category;
    private List<Long> user_list;
    private Long final_time;



}
