package site.code4fun.repository.jpa;

import site.code4fun.model.LeaveRequestEntity;

import java.util.List;

public interface LeaveRepository extends BaseRepository<LeaveRequestEntity, Long> {
    List<LeaveRequestEntity> findByCreatedBy_Id(Long id);
}
