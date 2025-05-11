package site.code4fun.service.shipping;

import site.code4fun.model.OrderEntity;
import site.code4fun.service.shipping.ghn.dto.*;

import java.util.List;

public interface ShippingService {
    OrderResponse createOrder(OrderEntity order);

    List<ProvinceResponse> getProvinces();

    List<DistrictResponse> getDistricts(int provinceId);

    List<WardResponse> getWards(int districtId);
     default boolean isValid(OrderEntity order) {
        try {
            Integer.parseInt(order.getShippingAddress().getDistrict());
            Integer.parseInt(order.getShippingAddress().getProvince());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    CalculateFeeResponse calculateFee(OrderEntity order);
}
