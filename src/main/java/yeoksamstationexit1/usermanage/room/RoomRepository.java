package yeoksamstationexit1.usermanage.room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<RoomEntity, Long> {


//    @Query("SELECT r.category,r.processivity,r.submitNumber," +
//            "r.fixDay,r.fixPlace,r.fixStation,r.periodStart,r.periodEnd, " +
//            "p.progress,p.startpoint,p.roomName, " +
//            "u.nickname " +
//            "FROM ParticipantEntity p " +
//            "JOIN p.user u " +
//            "JOIN p.room r " +
//            "WHERE r.roomId = :roomId")
//    List<FindAllFromRoomDTO> findAllThingAboutRoom(@Param("roomId") Long roomId);

    Optional<RoomEntity> findByUUID(String UUID);


}
