package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.StockSymbolEntity;
import site.code4fun.model.dto.StockSymbolDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface StockSymbolMapper extends BaseMapper<StockSymbolEntity, StockSymbolDTO>{

}