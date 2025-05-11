package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.CouponEntity;
import site.code4fun.model.dto.CouponDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring", uses = {UserMapper.class})
public interface CouponMapper extends BaseMapper<CouponEntity, CouponDTO>{

}