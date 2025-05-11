package site.code4fun.repository.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import site.code4fun.constant.Status;
import site.code4fun.model.CategoryEntity ;

public interface CategoryRepository extends BaseRepository<CategoryEntity , Long> {

  Page<CategoryEntity> findAllByStatusEquals(Status status, Pageable pageReq);
}
