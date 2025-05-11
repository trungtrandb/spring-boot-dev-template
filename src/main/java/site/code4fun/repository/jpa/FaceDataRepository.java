package site.code4fun.repository.jpa;

import site.code4fun.model.FaceDataEntity;

import java.util.List;

public interface FaceDataRepository extends BaseRepository<FaceDataEntity, Long> {


    List<FaceDataEntity> findByProviderId(String id);
    List<FaceDataEntity> findByFbId(String id);
}

