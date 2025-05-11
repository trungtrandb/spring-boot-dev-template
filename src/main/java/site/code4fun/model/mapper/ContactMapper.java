package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.ContactEntity;
import site.code4fun.model.dto.ContactDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface ContactMapper extends BaseMapper<ContactEntity, ContactDTO>{

}