package yeoksamstationexit1.usermanage.room.roomDTO.request;

import lombok.Data;
import lombok.ToString;
import yeoksamstationexit1.usermanage.room.enumClass.Category;

import java.time.LocalDate;

@Data
@ToString
public class CreateRoomDTO {

    private String roomName;

    private Category category;

    private LocalDate start;

    private LocalDate end;
}
