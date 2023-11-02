package yeoksamstationexit1.usermanage.room.roomDTO.request;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@ToString
@Data
public class FixDateDTO {
    private Long roomId;

    private LocalDate date;

}
