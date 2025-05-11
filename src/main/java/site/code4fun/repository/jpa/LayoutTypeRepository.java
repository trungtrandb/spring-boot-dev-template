package site.code4fun.repository.jpa;

import site.code4fun.model.LayoutTypeEntity;
import site.code4fun.constant.Status;

import java.util.List;
import java.util.Optional;

public interface LayoutTypeRepository extends BaseRepository<LayoutTypeEntity, Long> {
    Optional<LayoutTypeEntity> findBySlug(String slug);

    List<LayoutTypeEntity> findAllByStatus(Status status);
}

