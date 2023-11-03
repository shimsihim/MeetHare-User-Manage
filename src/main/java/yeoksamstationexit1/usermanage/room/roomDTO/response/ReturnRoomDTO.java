package yeoksamstationexit1.usermanage.room.roomDTO.response;

import lombok.*;
import yeoksamstationexit1.usermanage.room.enumClass.Category;
import yeoksamstationexit1.usermanage.room.enumClass.Processivity;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDate;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRoomDTO {//방입장시 기본 정보들

    private Category category;
    private Processivity processivity; // 방의 진행상황

    private LocalDate fixDay;  // 만날 날

    private String fixPlace; // 만날 장소

    private String fixStation; // 만날 장소

    private int submitNumber; // 시간과 장소 제출 인원

    private int number;

    private String periodStart;

    private String periodEnd;

    private String master;
}
