package site.code4fun.service.shipping.ghn;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import site.code4fun.constant.PaymentMethod;
import site.code4fun.constant.ShippingMethod;
import site.code4fun.exception.ServiceException;
import site.code4fun.model.OrderEntity;
import site.code4fun.model.dto.ProductSize;
import site.code4fun.service.shipping.ShippingService;
import site.code4fun.service.shipping.ghn.dto.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class Ghn implements ShippingService {
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    public Ghn(ObjectMapper mapper,
                      @Value("${shipping.ghn.base-url}") String baseUrl,
                      @Value("${shipping.ghn.token}") String apiToken,
                      @Value("${shipping.ghn.shop-id}") String shopId) {
        restTemplate = new RestTemplateBuilder()
                .rootUri(baseUrl)
                .defaultHeader("Token", apiToken)
                .defaultHeader("ShopId", shopId)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.mapper = mapper;
    }


    @SneakyThrows
    @Override
    public OrderResponse createOrder(OrderEntity order) {
        if (isValid(order)) {
            OrderRequest orderRequest = new OrderRequest();

            orderRequest.setToName(order.getName()); // Receiver
            orderRequest.setToPhone(order.getShippingAddress().getPhone());
            orderRequest.setToAddress(order.getShippingAddress().getAddress());
            orderRequest.setToWardCode(order.getShippingAddress().getWard());
            orderRequest.setToDistrictId(Integer.valueOf(order.getShippingAddress().getDistrict()));

            orderRequest.setServiceTypeId(2);
            orderRequest.setServiceId(0);
            orderRequest.setPaymentTypeId(1);
            orderRequest.setRequiredNote("CHOXEMHANGKHONGTHU");
            orderRequest.setNote(order.getNote());
            orderRequest.setClientOrderCode(order.getId().toString());

            List<Item> lstItem = new ArrayList<>();
            AtomicInteger weight = new AtomicInteger(0);
            AtomicInteger width = new AtomicInteger(0);
            AtomicInteger height = new AtomicInteger(0);
            AtomicInteger length = new AtomicInteger(0);

            order.getItems().forEach(item -> {
                ProductSize size = item.getProductSize() != null ? item.getProductSize() : new ProductSize();

                Item reqItem = new Item();
                reqItem.setName(item.getName());
                reqItem.setQuantity(item.getQuantity());
                reqItem.setPrice(item.getPrice().intValue());
                reqItem.setCode(item.getId());

                // TODO this is not real, need to re-calculate
                weight.getAndAdd(size.getWeight());
                height.getAndAdd(size.getHeight());
                width.getAndAdd(size.getWidth());
                length.getAndAdd(size.getLength());

                lstItem.add(reqItem);
            });
            // default min avoid error
            orderRequest.setWeight(weight.get() > 0 ? weight.get() : 100);
            orderRequest.setWidth(width.get() > 0 ? width.get() : 100);
            orderRequest.setHeight(height.get() > 0 ? height.get() : 100);
            orderRequest.setLength(length.get() > 0 ? length.get() : 100);

            orderRequest.setItems(lstItem);

            if (order.getPaymentMethod() == PaymentMethod.COD) {
                orderRequest.setCodAmount(order.getTotal().intValue());
            }


            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(orderRequest));
            try{
                ResponseEntity<ResponseDTO<OrderResponse>> response = restTemplate.exchange("/v2/shipping-order/create", HttpMethod.POST, entity, new ParameterizedTypeReference<>() {
                });
                return response.getBody() != null ? response.getBody().getData() : null;
            }catch (HttpClientErrorException.BadRequest e){
                throw new ServiceException(e.getMessage());
            }

        }
        throw new ServiceException("Can't create order, please check parameters.");
    }

    @SneakyThrows
    @Override
    public List<ProvinceResponse> getProvinces() {
        ResponseEntity<ResponseDTO<List<ProvinceResponse>>> response = restTemplate.exchange("/master-data/province", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });
        if (response.getBody() != null) {
            return response.getBody().getData();
        }
        return Collections.emptyList();
    }

    @SneakyThrows
    @Override
    public List<DistrictResponse> getDistricts(int provinceId) {
        HttpEntity<?> httpEntity = new HttpEntity<>(mapper.writeValueAsString(Map.ofEntries(
                Map.entry("province_id", provinceId)
        )));
        ResponseEntity<ResponseDTO<List<DistrictResponse>>> response = restTemplate.exchange("/master-data/district", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<>() {
        });
        if (response.getBody() != null) {
            return response.getBody().getData();
        }
        return Collections.emptyList();
    }

    @SneakyThrows
    @Override
    public List<WardResponse> getWards(int districtId) {
        HttpEntity<?> httpEntity = new HttpEntity<>(mapper.writeValueAsString(Map.ofEntries(
                Map.entry("district_id", districtId)
        )));
        ResponseEntity<ResponseDTO<List<WardResponse>>> response = restTemplate.exchange("/master-data/ward?district_id", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<>() {
        });
        if (response.getBody() != null) {
            return response.getBody().getData();
        }
        return Collections.emptyList();
    }

    @SneakyThrows
    @Override
    public CalculateFeeResponse calculateFee(OrderEntity order) { // TODO move to shippingProviderFactory
        if (order.getShippingMethod() == ShippingMethod.GHN
                || order.getShippingMethod() == ShippingMethod.GHTK && isValid(order)) {
            CalculateFeeRequest request = new CalculateFeeRequest();
            request.setServiceTypeId(2);
            request.setToWardCode(order.getShippingAddress().getWard());
            request.setToDistrictId(Integer.parseInt(order.getShippingAddress().getDistrict()));
            request.setWeight(1000);

            HttpEntity<?> entity = new HttpEntity<>(mapper.writeValueAsString(request));
            ResponseEntity<ResponseDTO<CalculateFeeResponse>> response = restTemplate.exchange("/v2/shipping-order/fee", HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
            });
            if (response.getBody() != null) {
                return response.getBody().getData();
            }
        }
        return null;
    }
}
