package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.dto.RoleDTO;
import site.code4fun.model.Role;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface RoleMapper extends BaseMapper<Role, RoleDTO> {
}
