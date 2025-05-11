package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.OrderItem;
import site.code4fun.model.dto.ProductDTO;
import site.code4fun.model.Product;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface ProductMapper extends BaseMapper<Product, ProductDTO>{

    @Mapping(target = "id", ignore = true)
    OrderItem entityToOrderItem(Product p);
}