package yeoksamstationexit1.usermanage.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yeoksamstationexit1.usermanage.user.dayType;
import yeoksamstationexit1.usermanage.user.entity.FixCalendarEntity;
import yeoksamstationexit1.usermanage.user.entity.FixCalendarId;

import java.util.List;
import java.util.Optional;

public interface FixCalendarRepository  extends JpaRepository<FixCalendarEntity, FixCalendarId> {

    // 사용자 ID를 기반으로 캘린더 조회
    Optional<List<FixCalendarEntity>> findByUserId(Long userId);

    // 사용자 ID 및 요일을 기반으로 캘린더 조회
    Optional<List<FixCalendarEntity>> findByUserIdAndDay(Long userId, dayType daytype);


}
