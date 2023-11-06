package yeoksamstationexit1.usermanage.room.roomDTO.request;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class NameChangeDTO {

    private long roomId;
    private String editedTitle;

}
