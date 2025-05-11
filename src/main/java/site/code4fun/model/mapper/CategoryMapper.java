package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.CategoryEntity;
import site.code4fun.model.dto.CategoryDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface CategoryMapper extends BaseMapper<CategoryEntity, CategoryDTO>{
}