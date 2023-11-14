package yeoksamstationexit1.usermanage.room.participant.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import yeoksamstationexit1.usermanage.room.enumClass.Processivity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantDTO {

    private Long id;
    private String nickName;
    private String stationName ;
    private double latitude ;
    private double longitude ;
    private Processivity progress;




}
