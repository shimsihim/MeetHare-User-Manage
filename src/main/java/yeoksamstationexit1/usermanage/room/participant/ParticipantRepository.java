package yeoksamstationexit1.usermanage.room.participant;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<ParticipantEntity, ParticipantEmbededId> {

    Optional<List<ParticipantEntity>> findByIdRoomId(Long roomId);
    ParticipantEntity findByIdUserIdAndIdRoomId(Long userId, Long roomId);

    List<ParticipantEntity> findByIdUserId(Long userId);



}
