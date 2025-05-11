package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.QuestionEntity;
import site.code4fun.model.dto.QuestionDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface QuestionMapper extends BaseMapper<QuestionEntity, QuestionDTO> {

    QuestionEntity requestToEntity(QuestionDTO dto);
}