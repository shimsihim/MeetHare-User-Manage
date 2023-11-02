package yeoksamstationexit1.usermanage.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yeoksamstationexit1.usermanage.user.dayType;
import yeoksamstationexit1.usermanage.user.entity.FixCalendarEntity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public interface FixCalendarRepository  extends JpaRepository<FixCalendarEntity, Long> {

    // 사용자 ID를 기반으로 캘린더 조회
    @Query("SELECT timelist FROM FixCalendarEntity timelist WHERE timelist.id.userId = :userId ORDER BY timelist.id.impossibleDate ASC")
    Optional<List<FixCalendarEntity>> findByIdUserIdOrderByImpossibleDateAsc(Long userId);

    @Query("SELECT DISTINCT fc.id.impossibleDate FROM FixCalendarEntity fc WHERE fc.id.userId IN :userIds")
    List<LocalDate> findByUserList(List<Long> userIds);

    @Query("SELECT DISTINCT fc.id.impossibleDate FROM FixCalendarEntity fc " +
            "WHERE fc.id.userId IN :userIds " +
            "AND fc.id.impossibleDate >= :startDate " +
            "AND fc.id.impossibleDate <= :endDate")
    List<LocalDate> findByUserListAndDateRange(@Param("userIds") List<Long> userIds,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);



}
