package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.OtpCodeEntity;
import site.code4fun.model.dto.OtpCode;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface OtpCodeMapper extends BaseMapper<OtpCodeEntity, OtpCode>{

}