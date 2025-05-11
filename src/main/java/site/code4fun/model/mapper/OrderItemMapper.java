package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.OrderItem;
import site.code4fun.model.dto.OrderItemDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface OrderItemMapper extends BaseMapper<OrderItem, OrderItemDTO>{
}