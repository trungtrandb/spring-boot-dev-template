package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.PromptEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface PromptMapper extends BaseMapper<PromptEntity, PromptEntity>{

}