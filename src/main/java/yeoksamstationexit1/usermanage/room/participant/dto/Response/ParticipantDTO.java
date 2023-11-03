package yeoksamstationexit1.usermanage.room.participant.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantDTO {

    private Long id;
    private String nickName;
    private String startpoint ;
    private double latitude ;
    private double longitude ;




}
