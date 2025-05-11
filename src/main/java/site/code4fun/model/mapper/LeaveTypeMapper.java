package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.LeaveTypeEntity;
import site.code4fun.model.dto.LeaveTypeDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface LeaveTypeMapper extends BaseMapper<LeaveTypeEntity, LeaveTypeDTO>{
}