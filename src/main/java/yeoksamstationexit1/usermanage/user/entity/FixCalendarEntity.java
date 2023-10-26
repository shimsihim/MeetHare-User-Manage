package yeoksamstationexit1.usermanage.user.entity;

import lombok.*;
import yeoksamstationexit1.usermanage.user.UserEntity;
import yeoksamstationexit1.usermanage.user.dayType;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;


@Getter
@Data
@ToString
@Table(name = "ImpossibleCalendar") // 테이블과 클래스명이 같을 경우 생략 가능
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class FixCalendarEntity {

    @EmbeddedId
    private FixCalendertId id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    @MapsId("userId")
    private UserEntity userEntity;




}
