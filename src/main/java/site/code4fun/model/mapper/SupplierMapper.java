package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.SupplierEntity;
import site.code4fun.model.dto.SupplierDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface SupplierMapper extends BaseMapper<SupplierEntity, SupplierDTO>{
}