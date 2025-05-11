package site.code4fun.repository.jpa;

import org.springframework.data.jpa.repository.Query;
import site.code4fun.model.AttendanceEntity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

public interface AttendanceRepository extends BaseRepository<AttendanceEntity, Long> {
    List<AttendanceEntity> findByUser_IdAndCreatedBetween(Long userId, LocalDateTime createdAfter, LocalDateTime createdBefore);
    List<AttendanceEntity> findByCreatedBetween(LocalDateTime createdAfter, LocalDateTime createdBefore);

    long countByCheckInIsNullAndCreatedBetweenAndUser_id(Date createdAfter, Date createdBefore, Long userId);
    long countByCheckInIsNotNullAndCreatedBetweenAndUser_id(Date firstDayInMonth, Date lastDayInMonth, Long userId);
    long countByCreatedBetweenAndUser_id(Date firstDayInMonth, Date lastDayInMonth, Long userId);

    @Query("SELECT COUNT(a) FROM AttendanceEntity a" +
            " WHERE a.checkIn IS NOT NULL" +
            " AND FUNCTION('TIME', a.checkIn) > :checkIn" +
            " AND a.user.id = :userId" +
            " AND a.created between :after and :before")
    long countByCheckInAfterAndCreatedBetween(LocalTime checkIn, Date after, Date before, Long userId);

    long countByWorkedTimeBetweenAndCreatedBetweenAndUser_id(long workedTime, long workedTime2, Date created, Date created2, Long user_id);
}

