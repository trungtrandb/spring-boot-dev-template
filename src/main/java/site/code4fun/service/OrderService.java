package site.code4fun.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.code4fun.constant.OrderStatus;
import site.code4fun.constant.PaymentMethod;
import site.code4fun.constant.SearchOperator;
import site.code4fun.exception.NotFoundException;
import site.code4fun.exception.ServiceException;
import site.code4fun.model.OrderEntity;
import site.code4fun.model.OrderItem;
import site.code4fun.model.PaymentTransactionEntity;
import site.code4fun.model.Product;
import site.code4fun.model.dto.GhnUpdateDTO;
import site.code4fun.model.dto.OrderDTO;
import site.code4fun.model.dto.ProductDTO;
import site.code4fun.model.dto.SearchCriteria;
import site.code4fun.model.mapper.OrderMapper;
import site.code4fun.model.mapper.ProductMapper;
import site.code4fun.model.request.OrderRequest;
import site.code4fun.repository.SearchSpecification;
import site.code4fun.repository.jpa.OrderRepository;
import site.code4fun.service.payment.PaymentFactory;
import site.code4fun.service.payment.PaymentProvider;
import site.code4fun.service.shipping.ShippingService;
import site.code4fun.service.shipping.ghn.dto.CalculateFeeResponse;
import site.code4fun.service.shipping.ghn.dto.OrderResponse;
import site.code4fun.util.SecurityUtils;
import site.code4fun.util.UrlParserUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static site.code4fun.constant.AppConstants.SEARCH_KEY;
import static site.code4fun.util.FormatUtils.formatBigDecimal;

@RequiredArgsConstructor
@Service
@Lazy
@Slf4j
public class OrderService extends AbstractBaseService<OrderEntity, Long> {
    @Getter
    private final OrderRepository repository;
    private final ProductService productService;
    private final OrderMapper mapper ;
    private final ProductMapper productMapper;
    private final InventoryService inventoryService;
    private final ShippingService shippingService;
    private final PaymentFactory paymentFactory;

    public OrderDTO getDtoById(Long id) {
        OrderDTO dto = mapper.entityToDto(super.getById(id));
        long countOrder = getRepository().countAllByStatusAndUser_id(OrderStatus.DELIVERED, dto.getUser().getId());
        dto.setPreviousOrder((int) countOrder);
        return dto;
    }

    public Page<OrderEntity> getPagingCurrentUser(Map<String, String> mapRequestParam){
        Long userId = SecurityUtils.getUserId();
        if (userId != null){
            return getPagingByUserId(mapRequestParam, userId);
        }
        throw new ServiceException("Access denied");
    }

    public Page<OrderEntity> getPagingByUserId(Map<String, String> mapRequestParam, Long userId) {
        Specification<OrderEntity> specification = null;
        Pageable pageReq = buildPageRequest(mapRequestParam);

        List<SearchCriteria> lstSearch = UrlParserUtils.parserQueryString(mapRequestParam, OrderEntity.class);

        if(isNotBlank(mapRequestParam.get(SEARCH_KEY)) ){
            specification = new SearchSpecification<>(new SearchCriteria("id", SearchOperator.EQUAL, mapRequestParam.get(SEARCH_KEY)));
            specification = specification.or(new SearchSpecification<>(new SearchCriteria("name", SearchOperator.EQUAL, mapRequestParam.get(SEARCH_KEY))));
        } else if (!lstSearch.isEmpty()) {
            specification = new SearchSpecification<>(lstSearch.get(0));
            for (SearchCriteria searchCriteria : lstSearch){
                specification = specification.and(new SearchSpecification<>(searchCriteria));
            }
        }
        if (null != userId){
            SearchCriteria searchCriteria = new SearchCriteria("user", SearchOperator.IN, userId);
            specification = specification == null
                    ? new SearchSpecification<>(searchCriteria)
                    : specification.and(new SearchSpecification<>(searchCriteria));
        }

        if (specification != null){
            return getRepository().findAll(specification, pageReq);
        }else {
            return getRepository().findAll(pageReq);
        }
    }

    @Override
    public Page<OrderEntity> getPaging(Map<String, String> mapRequestParam) {
        return getPagingByUserId(mapRequestParam, null);
    }

    public PaymentTransactionEntity payNow(Long orderId,  HttpServletRequest request, String paymentMethod){
        OrderEntity order = getById(orderId);
        PaymentTransactionEntity transaction = getExistedTransaction(order, paymentMethod).orElseGet(()->{
            OrderRequest orderRequest = mapper.entityToRequest(order);
            orderRequest.setPaymentMethod(paymentMethod);
            return paymentFactory.getProvider(paymentMethod).doPay(orderRequest, request);
        });
        if (transaction.getId() == null){
            order.addTransaction(transaction);
            getRepository().save(order);
        }
        return transaction;
    }

    private Optional<PaymentTransactionEntity> getExistedTransaction(OrderEntity order, String paymentMethod){
        List<PaymentTransactionEntity> transactions = order.getTransactions()
                .stream()
                .filter(
                        payment -> payment.getExpDate() == null // COD or BankTransfer
                        || payment.getExpDate().after(new Date()))  // VnPay, OnePay
                .toList();

        if (isNotBlank(paymentMethod)){
            return transactions.stream()
                    .filter(transaction -> PaymentMethod.valueOf(paymentMethod) == transaction.getPaymentMethod())
                    .findFirst();

        }else{
            return Optional.ofNullable(transactions.get(0));
        }
    }

    public OrderEntity createOrder(OrderRequest orderRequest, HttpServletRequest request){
        checkInventory(orderRequest, request);
        placeOrder(orderRequest, request);
        createPayment(orderRequest, request);
        return getById(orderRequest.getId());
    }

    @Transactional
    public OrderEntity changeOrderStatus(long id) {
        OrderEntity order =  getById(id);
        if (order.getStatus() == OrderStatus.PENDING){
            order.setStatus(OrderStatus.CONFIRMED);
            return getRepository().save(order);
        }

        if (order.getStatus() == OrderStatus.CONFIRMED){
            order.setStatus(OrderStatus.PROCESSING);
            return getRepository().save(order);
        }

        if (order.getStatus() == OrderStatus.PROCESSING){
            OrderResponse res = shippingService.createOrder(order);
            log.info("Ship order created, shipping service response {}", res.getExpected_delivery_time());
            order.setExpectedDeliveryTime(res.getExpected_delivery_time());
            order.getItems().forEach(orderItem -> productService.decreaseQuantity(orderItem.getProductId(), orderItem.getQuantity()));
            order.setStatus(OrderStatus.SHIPPED);
            return getRepository().save(order);
        }
        return order;
    }

    public OrderEntity rejectOrder(long id) {
        OrderEntity order =  getById(id);
        order.setStatus(OrderStatus.CANCELLED);
        return getRepository().save(order);
    }

    public void ghnHook(GhnUpdateDTO ipnRequest) {
        // TODO check with ship provider before update
        OrderEntity order =  getById(Long.valueOf(ipnRequest.getClientOrderCode()));
        if (order.getStatus() == OrderStatus.SHIPPED){
            order.setStatus(OrderStatus.DELIVERED);
            getRepository().save(order);
        }
    }

    public OrderEntity calculateOrder(OrderRequest orderRequest) { // tmp calculate, not save anything
        OrderEntity order =  mapper.requestToEntity(orderRequest);
        calculateOrder(order, orderRequest);
        return order;
    }

    public Object getStatistic() {
        String valueField = "value";
        String growField = "growShrink";

        Map<String, Map<String, Object>> mapRes = new HashMap<>();
        long totalUser = getRepository().count();

        mapRes.put("total", Map.ofEntries(
                Map.entry(valueField, totalUser))
        );

        long countActive = getRepository().countAllByStatus(OrderStatus.DELIVERED);
        mapRes.put("completed", Map.ofEntries(
                Map.entry(valueField, countActive),
                Map.entry(growField, countActive > 0 ? countActive * 100 / totalUser : 0))
        );

        long countNew = getRepository().countAllByStatus(OrderStatus.SHIPPED);
        mapRes.put("shipping", Map.ofEntries(
                Map.entry(valueField, countNew),
                Map.entry(growField, countNew > 0 ? countNew * 100 / totalUser : 0))
        );

        long canceled = getRepository().countAllByStatus(OrderStatus.CANCELLED);
        mapRes.put("canceled", Map.ofEntries(
                Map.entry(valueField, canceled),
                Map.entry(growField,canceled > 0 ? canceled * 100 / totalUser : 0) )
        );
        return mapRes;
    }

    private void checkInventory(@NonNull OrderRequest order, HttpServletRequest request) {
        if (order.getItems() != null && inventoryService.isProductAvailable(order.getItems())) {
            log.info("Product is available. Handling next step");
        } else {
            log.warn("Cart is empty or Product is out of stock.");
            order.setStatus(OrderStatus.CANCELLED);
        }
    }

    public void placeOrder(OrderRequest orderRequest, HttpServletRequest request) {
        OrderEntity entity = mapper.requestToEntity(orderRequest);
        entity.setStatus(OrderStatus.PENDING);
        entity.getItems().clear();
        calculateOrder(entity, orderRequest);
        if (!SecurityUtils.isAdmin() || entity.getUser() == null){
            entity.setUser(SecurityUtils.getUser());
        }
        if (!SecurityUtils.isAdmin()){
            entity.setTax(BigDecimal.ZERO);
            entity.setDiscount(BigDecimal.ZERO);
        }
        PaymentMethod paymentMethod = PaymentMethod.valueOf(orderRequest.getPaymentMethod());
        entity.setPaymentMethod(paymentMethod);

        getRepository().save(entity);
        orderRequest.setId(entity.getId());
        orderRequest.setTotal(entity.getTotal());
        log.info("Order placed successfully.");
    }

    private void calculateOrder(OrderEntity entity, OrderRequest orderRequest){
        Set<Long> productIds = orderRequest.getItems().stream().map(ProductDTO::getId).collect(Collectors.toSet());

        AtomicReference<BigDecimal> subTotal = new AtomicReference<>(BigDecimal.valueOf(0));
        AtomicReference<BigDecimal> totalDiscount = new AtomicReference<>(formatBigDecimal(orderRequest.getDiscount()));
        Map<Long, Product> mapProduct = productService.findAllById(productIds).stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        orderRequest.getItems().forEach(orderItem -> {
            Product p = mapProduct.get(orderItem.getId());
            if (p != null){
                if (p.getQuantity() < orderItem.getQuantity()) {
                    throw new ServiceException(String.format("Product %s out of stock", p.getName()));
                }

                BigDecimal total = p.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
                BigDecimal discount = p.getDiscount() != null ? p.getDiscount().multiply(BigDecimal.valueOf(orderItem.getQuantity())) : BigDecimal.ZERO;

                totalDiscount.getAndUpdate(v -> v.add(discount));
                subTotal.getAndUpdate(v -> v.add(total));

                OrderItem item = productMapper.entityToOrderItem(p);
                item.setProductId(p.getId());
                item.setQuantity(orderItem.getQuantity());
                item.setTotal(total);

                entity.addItem(item);
            }else {
                throw new NotFoundException();
            }
        });
        CalculateFeeResponse fees = shippingService.calculateFee(entity);
        entity.setFees(fees != null ? BigDecimal.valueOf(fees.getTotal()) : BigDecimal.ZERO);

        entity.setDiscount(formatBigDecimal(totalDiscount.get()));
        entity.setSubTotal((subTotal.get()));

        BigDecimal taxValue = formatBigDecimal(entity.getTax()).divide(BigDecimal.valueOf(100), RoundingMode.DOWN).multiply(entity.getSubTotal());

        BigDecimal totalNeedPay = subTotal.get()
                .add(taxValue)
                .add(formatBigDecimal(entity.getFees()))
                .subtract(formatBigDecimal(entity.getDiscount()));
        entity.setTotal(totalNeedPay);
    }

    public void createPayment(OrderRequest order, HttpServletRequest request) {

        if (!"COD".equalsIgnoreCase(order.getPaymentMethod())) {
            OrderEntity orderEntity = getRepository().getReferenceById(order.getId());
            try{
                PaymentProvider provider = paymentFactory.getProvider(order.getPaymentMethod());

                if (provider != null) {
                    var paymentTransaction = provider.doPay(order, request);
                    orderEntity.addTransaction(paymentTransaction);
                    log.info("Create transaction successful.");
                } else {
                    log.warn("Payment failed.");
                    order.setStatus(OrderStatus.FAILED);
                }
            }catch (Exception e){
                log.warn(e.getMessage());
            }
            getRepository().save(orderEntity);
        }
    }
}
