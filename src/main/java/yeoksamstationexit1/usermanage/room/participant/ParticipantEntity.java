package yeoksamstationexit1.usermanage.room.participant;

import lombok.*;
import yeoksamstationexit1.usermanage.room.RoomEntity;
import yeoksamstationexit1.usermanage.room.enumClass.Processivity;
import yeoksamstationexit1.usermanage.user.UserEntity;


import javax.persistence.*;

@Table(name = "Participant")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Setter
@ToString
public class ParticipantEntity {




    @EmbeddedId
    private ParticipantEmbededId id;

    @ManyToOne
    @MapsId("roomId") // roomId를 기본 키로 사용
    @JoinColumn(name = "room_id")
    private RoomEntity room;

    @ManyToOne
    @MapsId("userId") // roomId를 기본 키로 사용
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private Processivity progress;

    @Column(nullable = true)
    public String startpoint;

    public String roomName;

    public ParticipantEntity(ParticipantEmbededId id ) {
        this.id = id;

    }

    public ParticipantEntity(UserEntity user,RoomEntity room , String startpoint) {
        this.user = user;
        this.room = room;
        this.startpoint = startpoint;
    }


    public void setPoint(String startpoint) {

        this.startpoint = startpoint;
    }

//    public void setImpossibleTimeData(String json) {
//
//        this.impossibleTimeData = json;
//    }


}
