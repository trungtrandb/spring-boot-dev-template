package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.LayoutTypeEntity;
import site.code4fun.model.dto.LayoutTypeDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface LayoutTypeMapper extends BaseMapper<LayoutTypeEntity, LayoutTypeDTO>{

}