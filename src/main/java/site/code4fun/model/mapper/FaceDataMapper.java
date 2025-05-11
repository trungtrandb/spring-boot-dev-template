package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.FaceDataEntity;
import site.code4fun.model.dto.FaceDataDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface FaceDataMapper extends BaseMapper<FaceDataEntity, FaceDataDTO>{
}