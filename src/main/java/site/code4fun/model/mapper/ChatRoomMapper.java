package site.code4fun.model.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.ChatRoomEntity;
import site.code4fun.model.dto.ChatRoomDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface ChatRoomMapper extends BaseMapper<ChatRoomEntity, ChatRoomDTO>{

    @AfterMapping
    default void lastMessage(@MappingTarget ChatRoomDTO dto, ChatRoomEntity entity) {
        dto.setAvatar(entity.getCreatedBy().getAvatar());
    }
}