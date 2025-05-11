package site.code4fun.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.OrderEntity;
import site.code4fun.model.dto.*;
import site.code4fun.model.mapper.OrderMapper;
import site.code4fun.model.mapper.PaymentTransactionMapper;
import site.code4fun.model.request.OrderRequest;
import site.code4fun.service.LayoutService;
import site.code4fun.service.OrderService;
import site.code4fun.service.payment.PaymentFactory;
import site.code4fun.service.shipping.ShippingService;
import site.code4fun.service.shipping.ghn.dto.DistrictResponse;
import site.code4fun.service.shipping.ghn.dto.ProvinceResponse;
import site.code4fun.service.shipping.ghn.dto.WardResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(AppEndpoints.ORDERS_ENDPOINT)
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Lazy
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final PaymentFactory paymentService;
    private final OrderMapper mapper;
    private final PaymentTransactionMapper transactionMapper;
    private final LayoutService layoutService;
    private final ShippingService shippingService;
    @PostMapping
    @Transactional
    public OrderDTO createOrder(@RequestBody OrderRequest body, HttpServletRequest request){
        return mapper.entityToDto(orderService.createOrder(body, request));
    }

    @GetMapping
    @Transactional
    public Page<OrderDTO> getAllPaging(@RequestParam Map<String, String> mapRequest) {
        return orderService.getPagingCurrentUser(mapRequest).map(mapper::entityToDto);
    }

    @GetMapping("/{id}")
    @PostAuthorize("returnObject.user.id == authentication.principal.id")
    public OrderDTO getById(@PathVariable long id){
        return orderService.getDtoById(id);
    }

    @GetMapping("/{id}/pay-now")
    @Transactional
    public PaymentTransactionDTO payNow(@PathVariable long id, @RequestParam String paymentMethod, HttpServletRequest request){
        return transactionMapper.entityToDto(orderService.payNow(id, request, paymentMethod));
    }

    @GetMapping("/payment-methods")
    @Cacheable("PaymentMethods")
    public List<PaymentMethodDTO> getPaymentMethod(){
        return paymentService.getPaymentMethods(layoutService.getSetting());
    }

    @GetMapping("/provinces")
    public List<ProvinceResponse> getProvinces(){
        return shippingService.getProvinces();
    }

    @GetMapping("/provinces/{id}/districts")
    public List<DistrictResponse> getDistricts(@PathVariable int id){
        return shippingService.getDistricts(id);
    }

    @GetMapping("/districts/{id}/wards")
    public List<WardResponse> getWards(@PathVariable int id){
        return shippingService.getWards(id);
    }

    @PostMapping("/verify")
    @Transactional
    public ResponseEntity<VerifiedResponseDTO> verifyBeforeCheckout(@RequestBody OrderRequest body){
        OrderEntity entity = orderService.calculateOrder(body);
        VerifiedResponseDTO response = new VerifiedResponseDTO(entity.getFees(), entity.getFees(), null, new BigDecimal(1000L), new BigDecimal(1000L));
        return ResponseEntity.ok(response);
    }


    @GetMapping("/ipn/vnpay") // Instant Payment Notification
    @Transactional
    public ResponseEntity<Void> doIpn(@RequestParam Map<String, String> ipnRequest){
        log.info(ipnRequest.toString());
        paymentService.getProvider("VNPAY").doIpn(ipnRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ipn/onepay")
    @Transactional
    public ResponseEntity<Void> onepayIpn(@RequestParam Map<String, String> ipnRequest){
        log.info(ipnRequest.toString());
        paymentService.getProvider("ONEPAY").doIpn(ipnRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ipn/sepay")
    @Transactional
    public ResponseEntity<Map<String, Boolean>> sePayIpn(@RequestBody Map<String, String> ipnRequest){
        try{
            log.info(ipnRequest.toString());
            paymentService.getProvider("SEPAY").doIpn(ipnRequest);
            return ResponseEntity.ok().body(Map.ofEntries(
                    Map.entry("success", true)));
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.ok().body(Map.ofEntries(
                    Map.entry("success", false)));
        }

    }

    @PostMapping("/ipn/ghn")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Map<String, Boolean>> ghnHook(@RequestBody GhnUpdateDTO ipnRequest){
        try{
            log.info(ipnRequest.toString());
            orderService.ghnHook(ipnRequest);
            return ResponseEntity.ok().body(Map.ofEntries(
                    Map.entry("success", true)));
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.ok().body(Map.ofEntries(
                    Map.entry("success", false)));
        }
    }
}
