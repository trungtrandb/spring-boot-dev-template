package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.InventoryEntity;
import site.code4fun.model.dto.InventoryDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface InventoryMapper extends BaseMapper<InventoryEntity, InventoryDTO>{
}