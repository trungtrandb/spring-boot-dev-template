package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.OrderEntity;
import site.code4fun.model.dto.OrderDTO;
import site.code4fun.model.request.OrderRequest;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface OrderMapper extends BaseMapper<OrderEntity, OrderDTO>{
    OrderRequest entityToRequest(OrderEntity source);
    @Mapping(target = "paymentMethod", ignore = true)
    OrderEntity requestToEntity(OrderRequest source);


}