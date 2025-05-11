package site.code4fun.repository.jpa;


import site.code4fun.model.AppSettingEntity;

import java.util.Optional;

public interface AppSettingRepository extends BaseRepository<AppSettingEntity, Long> {

    Optional<AppSettingEntity> findFirstByKey(String key);
}
