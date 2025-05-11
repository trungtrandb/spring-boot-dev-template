package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.TagEntity;
import site.code4fun.model.dto.TagDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface TagMapper extends BaseMapper<TagEntity, TagDTO>{
}