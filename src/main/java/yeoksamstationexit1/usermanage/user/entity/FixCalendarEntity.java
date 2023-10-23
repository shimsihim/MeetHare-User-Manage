package yeoksamstationexit1.usermanage.user.entity;

import lombok.*;
import yeoksamstationexit1.usermanage.user.UserEntity;
import yeoksamstationexit1.usermanage.user.dayType;

import javax.persistence.*;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "FixCalendar") // 테이블과 클래스명이 같을 경우 생략 가능
@Entity
@IdClass(FixCalendarId.class)// 복합키를 위한 설정
public class FixCalendarEntity {

  @Id
  @ManyToOne
  @JoinColumn(name = "user_id")
  private UserEntity user; // 유저 아이디

  @Id
  @Enumerated(EnumType.STRING)
  @Column(name = "day")
  private dayType day; // 월요일부터 일요일까지의 값

  @Column(name = "time")
  private long time; // 시간을 long으로 저장




}
