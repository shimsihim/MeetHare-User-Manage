package yeoksamstationexit1.usermanage.room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yeoksamstationexit1.usermanage.room.enumClass.Category;
import yeoksamstationexit1.usermanage.room.enumClass.Processivity;
import yeoksamstationexit1.usermanage.room.roomDTO.CreateRoomDTO;

import javax.persistence.*;
import java.time.LocalDate;


@AllArgsConstructor
@NoArgsConstructor
@Table(name = "room")
@Getter
@Entity
@Setter
public class RoomEntity {


    @Id
    @Column(name = "room_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    private Category category;

    private Processivity processivity; // 방의 진행상황

    private LocalDate fixDay;  // 만날 날

    private String fixPlace; // 만날 장소

    private String fixStation; // 만날 장소


    private int submitNumber; // 시간과 장소 제출 인원

    private LocalDate periodStart;

    private LocalDate periodEnd;

    public RoomEntity(CreateRoomDTO dto) {
        this.category = dto.getCategory();
        this.periodStart = dto.getStartDate();
        this.periodEnd = dto.getEndDate();
        this.processivity = Processivity.InSubmission;
    }
}
