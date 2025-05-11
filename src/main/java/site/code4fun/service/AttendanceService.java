package site.code4fun.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.code4fun.constant.DayOfMonth;
import site.code4fun.constant.SearchOperator;
import site.code4fun.model.AttendanceEntity;
import site.code4fun.model.ShiftEntity;
import site.code4fun.model.User;
import site.code4fun.model.dto.SearchCriteria;
import site.code4fun.model.mapper.UserMapper;
import site.code4fun.repository.jpa.AttendanceRepository;
import site.code4fun.repository.jpa.UserRepository;
import site.code4fun.util.ExcelUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static site.code4fun.util.DateTimeUtils.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class AttendanceService extends AbstractBaseService<AttendanceEntity, Long> {

    private final AttendanceRepository repository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final LeaveRequestService leaveRequestService;
    private final ShiftService shiftService;
    private final UserRepository userRepository;

    @Override
    @SneakyThrows
    public AttendanceEntity create(AttendanceEntity ignored) {

        AttendanceEntity attendanceEntity = getTodayAttendance();
        if (attendanceEntity == null){
            attendanceEntity = new AttendanceEntity();
            attendanceEntity.setUser(userService.getCurrentUser());
        }

        if (attendanceEntity.getCheckIn() == null){
            attendanceEntity.setCheckIn(LocalDateTime.now());
        }else{
            attendanceEntity.setCheckOut(LocalDateTime.now());
        }
        attendanceEntity.setStatus(AttendanceEntity.Status.PRESENT);
        calculateOvertimeAndBreakTime(attendanceEntity);
        calculateWorkingTime(attendanceEntity);
        return getRepository().save(attendanceEntity);
    }

    public AttendanceEntity getTodayAttendance() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        List<AttendanceEntity> lst = getRepository().findByUser_IdAndCreatedBetween(userService.getCurrentUser().getId(), startOfDay, endOfDay);
        return  lst.isEmpty() ? null : lst.get(0);
    }

    private void calculateWorkingTime(AttendanceEntity attendanceEntity){
        if (attendanceEntity.getCheckOut() != null) {
            long shiftTime = Duration.between(attendanceEntity.getCheckIn(), attendanceEntity.getCheckOut()).toMinutes();
            attendanceEntity.setWorkedTime(shiftTime - attendanceEntity.getBreakTime());
        }
    }

    private void calculateOvertimeAndBreakTime(AttendanceEntity attendanceEntity){
        ShiftEntity shift = attendanceEntity.getUser().getShift();

        if (shift != null) {
            long breakTime = 0;
            LocalDateTime checkIn = attendanceEntity.getCheckIn();
            LocalDateTime checkOut = attendanceEntity.getCheckOut();

           // Calculate break time deductions
            if (shift.getBreakStartTime() != null && shift.getBreakEndTime() != null) {
                LocalDateTime breakStart = shift.getBreakStartTime().atDate(checkIn.toLocalDate());
                LocalDateTime breakEnd = shift.getBreakEndTime().atDate(checkIn.toLocalDate());

                // Calculate break time within working hours
                LocalDateTime actualBreakStart = checkIn.isAfter(breakStart) ? checkIn : breakStart;
                LocalDateTime actualBreakEnd = breakEnd;
                if (checkOut != null && checkOut.isBefore(breakEnd)) {
                     actualBreakEnd = checkOut;
                }

                if (actualBreakStart.isBefore(actualBreakEnd)) {
                    breakTime = Duration.between(actualBreakStart, actualBreakEnd).toMinutes();
                }
                attendanceEntity.setBreakTime(breakTime > 0 ? breakTime : 0);
            }

            // Calculate overtime
            LocalDateTime checkout = attendanceEntity.getCheckOut() != null ? attendanceEntity.getCheckOut() : LocalDateTime.now();
            long workedTimeWithBreak = Duration.between(attendanceEntity.getCheckIn(), checkout).toMinutes();
            long workedTimeWithoutBreak = workedTimeWithBreak - attendanceEntity.getBreakTime();
            long overTime = workedTimeWithoutBreak - getStandardWorkingTime(shift);
            attendanceEntity.setOverTime(overTime > 0 ? overTime : 0);
        }
    }


    @Override
    protected Specification<AttendanceEntity> createSpecification(List<SearchCriteria> criteriaList, String queryString) {
        for (SearchCriteria criteria : criteriaList) {
            if (criteria.getKey().equals("user")) {
                criteria.setOperation(SearchOperator.IN);
            }
        }
        return super.createSpecification(criteriaList, queryString);
    }

    @Override
    public ByteArrayResource export() {
        List<AttendanceEntity> lstEntity = getRepository().findAll();
        return ExcelUtils.export(lstEntity, Map.ofEntries(
                Map.entry("checkin", "Check In"),
                Map.entry("checkout", "Check Out")
        ));
    }

    @Transactional
    public void createAttendanceEveryDay() { // TODO create job for all shift
        long shiftId = 4;
        String dayString = getDayAsString(LocalDate.now());
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        List<Long> usersCheckedIn = getRepository().findByCreatedBetween(startOfDay, endOfDay).stream().map(AttendanceEntity::getUser).map(User::getId).toList();

        ShiftEntity shift = shiftService.getRepository().findById(shiftId).orElse(null);

        if (shift != null && shift.getWorkingDays().contains(DayOfMonth.valueOf(dayString))) {
            List<User> users = userRepository.findAllByShift_Id(shift.getId());
            List<AttendanceEntity> lst = new ArrayList<>();
            for (User user : users) {
                if(usersCheckedIn.contains(user.getId())) continue;
                AttendanceEntity attendanceEntity = new AttendanceEntity();
                attendanceEntity.setUser(user);
                attendanceEntity.setStatus(shift.getDefaultStatusWhenNoCheckin());
                if (shift.getDefaultStatusWhenNoCheckin() == AttendanceEntity.Status.PRESENT) {
                    attendanceEntity.setWorkedTime(getStandardWorkingTime(shift));
                }
                lst.add(attendanceEntity);
            }
            getRepository().saveAll(lst);
        }
    }

    private long getStandardWorkingTime(ShiftEntity shift) {
        long workingTime = 8;
        if (shift != null) {
            if (shift.getStartTime() != null && shift.getEndTime() != null) {
                workingTime = Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes();
            }
            if (shift.getBreakStartTime() != null && shift.getBreakEndTime() != null) {
                workingTime = workingTime - Duration.between(shift.getBreakStartTime(), shift.getBreakEndTime()).toMinutes();
            }
        }

        return workingTime;
    }

    public Object getStatistic() { // TODO test only
        User user = userService.getCurrentUser();
        Map<String, Object> mapRes = new HashMap<>();

        if (user == null) return mapRes;
        Date firstDayInMonth = Date.from(getFirstDateOfMonth().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date lastDayInMonth = Date.from(getLastDateOfMonth().atStartOfDay(ZoneId.systemDefault()).toInstant());

        long lateCheckinCount = 0;
        long haftWorkCount = 0;
        if (user.getShift() != null) {
            LocalTime startTime = user.getShift().getStartTime();
            lateCheckinCount = getRepository().countByCheckInAfterAndCreatedBetween(startTime, firstDayInMonth, lastDayInMonth, user.getId());
            long shiftWorkTime = getStandardWorkingTime(user.getShift());
            haftWorkCount = getRepository().countByWorkedTimeBetweenAndCreatedBetweenAndUser_id(0, shiftWorkTime/2, firstDayInMonth, lastDayInMonth, user.getId());
        }

        long absentCount = getRepository().countByCheckInIsNullAndCreatedBetweenAndUser_id(firstDayInMonth, lastDayInMonth, user.getId());
        long presentCount = getRepository().countByCheckInIsNotNullAndCreatedBetweenAndUser_id(firstDayInMonth, lastDayInMonth, user.getId());
        long totalWorkingDay = getRepository().countByCreatedBetweenAndUser_id(firstDayInMonth, lastDayInMonth, user.getId());

        mapRes.put("haftDay", haftWorkCount);
        mapRes.put("lateDay", lateCheckinCount);
        mapRes.put("absentDay", absentCount);
        mapRes.put("presentDay", presentCount);
        mapRes.put("totalWorkingDay", totalWorkingDay);

        return mapRes;
    }
}
