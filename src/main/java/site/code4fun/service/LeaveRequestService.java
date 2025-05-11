package site.code4fun.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import site.code4fun.model.LeaveRequestEntity;
import site.code4fun.repository.jpa.EventRepository;
import site.code4fun.repository.jpa.LeaveRepository;

import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Service
@RequiredArgsConstructor
@Lazy
@Getter
public class LeaveRequestService extends AbstractBaseService<LeaveRequestEntity, Long> {
    private final LeaveRepository repository;
    private final EventRepository eventRepository;

    public long getLeaveHoursByUser(Long userId) {
        long totalHours = 0;
        List<LeaveRequestEntity> leaveRequests = getRepository().findByCreatedBy_Id(userId);
        for (LeaveRequestEntity request : leaveRequests) {
            Date fromDate = request.getFromDate();
            Date toDate = request.getToDate();
            long differenceInMillis = toDate.getTime() - fromDate.getTime();
            long differenceInDays = differenceInMillis / (1000 * 60 * 60 * 24);

            totalHours += differenceInDays * 8;
        }
        return totalHours;
    }

    @Override
    public LeaveRequestEntity update(Long id, LeaveRequestEntity entity) {
        LeaveRequestEntity dbEntity = getById(id);
        if (isNotEmpty(entity.getName())) {
            dbEntity.setName(entity.getName());
        }
        if (entity.getLeaveType() != null) {
            dbEntity.setLeaveType(entity.getLeaveType());
        }

        if (entity.getFromDate() != null) {
            dbEntity.setFromDate(entity.getFromDate());
        }

        if (entity.getToDate() != null) {
            dbEntity.setToDate(entity.getToDate());
        }

        if (entity.getStatus() != null) { //TODO check role approve of context user
            dbEntity.setStatus(entity.getStatus());
        }

        return getRepository().save(dbEntity);
    }
}
