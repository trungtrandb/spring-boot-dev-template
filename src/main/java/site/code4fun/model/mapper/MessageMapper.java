package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.MessageEntity;
import site.code4fun.model.dto.MessageDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface MessageMapper{

    MessageDTO entityToDto(MessageEntity messageEntity);
    MessageEntity dtoToEntity(MessageDTO messageEntity);
}
