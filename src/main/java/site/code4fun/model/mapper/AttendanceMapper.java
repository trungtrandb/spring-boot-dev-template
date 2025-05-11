package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.AttendanceEntity;
import site.code4fun.model.dto.AttendanceDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring", uses = {UserMapper.class})
public interface AttendanceMapper extends BaseMapper<AttendanceEntity, AttendanceDTO>{

}