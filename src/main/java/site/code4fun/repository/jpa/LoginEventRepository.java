package site.code4fun.repository.jpa;

import site.code4fun.model.LoginEventEntity;

public interface LoginEventRepository extends BaseRepository<LoginEventEntity, Long>{
    void deleteByCreatedBy_id(Long id);
}
