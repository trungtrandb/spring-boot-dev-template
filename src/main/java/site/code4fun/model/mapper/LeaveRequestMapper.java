package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.LeaveRequestEntity;
import site.code4fun.model.dto.LeaveRequestDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring", uses = {UserMapper.class})
public interface LeaveRequestMapper extends BaseMapper<LeaveRequestEntity, LeaveRequestDTO>{
}