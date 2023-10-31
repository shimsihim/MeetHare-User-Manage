package yeoksamstationexit1.usermanage.room.roomDTO.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class addDeleteDayListDTO {
    private Long roomId;
    private List<LocalDate> addDayList;
    private List<LocalDate> deleteDayList;
}
