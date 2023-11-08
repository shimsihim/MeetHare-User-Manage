package yeoksamstationexit1.usermanage.room.participant;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@ToString
@Data
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantEmbededId implements Serializable {
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "room_id")
    private Long roomId;
}
