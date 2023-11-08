package yeoksamstationexit1.usermanage.room.participant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<ParticipantEntity, ParticipantEmbededId> {

    Optional<List<ParticipantEntity>> findByIdRoomId(Long roomId);
    Optional<ParticipantEntity> findByIdUserIdAndIdRoomId(Long userId, Long roomId);

    List<ParticipantEntity> findByIdUserId(Long userId);

    @Query("SELECT p FROM ParticipantEntity p " +
            "WHERE p.user.id = :userId AND p.room.UUID = :uuid")
    Optional<ParticipantEntity> findByIdUserIdAndIdUUID(@Param("userId") Long userId, @Param("uuid") String uuid);



}
