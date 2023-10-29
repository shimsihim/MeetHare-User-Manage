package yeoksamstationexit1.usermanage.room.roomDTO.response;

import lombok.Data;
import lombok.ToString;
import yeoksamstationexit1.usermanage.room.enumClass.Processivity;

@Data
@ToString
public class ReturnRoomDTO {//방입장시 기본 정보들

    private String nickname;
    private Processivity personalProgress;
    private String roomName;




}
