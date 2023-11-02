package yeoksamstationexit1.usermanage.room.roomDTO.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class GetAllTimeInRoomDTO {

    private Long roomId;
    private LocalDate periodStart;
    private LocalDate periodEnd;

}
