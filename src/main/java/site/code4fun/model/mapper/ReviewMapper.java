package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.ReviewEntity;
import site.code4fun.model.dto.ReviewDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface ReviewMapper extends BaseMapper<ReviewEntity, ReviewDTO> {

    ReviewEntity requestToEntity(ReviewDTO dto);
}