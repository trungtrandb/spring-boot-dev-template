package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.LoginEventEntity;
import site.code4fun.model.dto.LoginEventDTO;
import site.code4fun.model.dto.UserDTO;
import site.code4fun.model.dto.UserLite;
import site.code4fun.model.User;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        uses = {ShiftMapper.class}
)
public interface UserMapper extends BaseMapper<User, UserDTO>{
    LoginEventDTO loginEventToDTO(LoginEventEntity entity);
    UserLite entityToLite(User source);
}
