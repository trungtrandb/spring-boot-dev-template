package site.code4fun.service.payment;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import site.code4fun.ApplicationProperties;
import site.code4fun.constant.PaymentMethod;
import site.code4fun.constant.PaymentStatus;
import site.code4fun.model.OrderEntity;
import site.code4fun.model.PaymentTransactionEntity;
import site.code4fun.model.request.OrderRequest;
import site.code4fun.repository.jpa.OrderRepository;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static site.code4fun.constant.AppConstants.TIME_ZONE;
import static site.code4fun.util.EncryptUtils.hmacSHA512;
import static site.code4fun.util.RandomUtils.random;


/**
 * - Card information Test
 *         Card Type    Cardholder Name    Card Number             Date(MM/YY)     OTP/CVV
 *         NCB          NGUYEN VAN A        9704198526191432198     07/15       123456
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Lazy
public class Vnpay implements PaymentProvider {
    private final ApplicationProperties properties;
    private final SimpleDateFormat vnpDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private final OrderRepository orderRepository;
    private static final String VNP_VERSION_FIELD = "vnp_Version";
    private static final String VNP_COMMAND_FIELD = "vnp_Command";
    private static final String VNP_TMN_CODE_FIELD = "vnp_TmnCode";
    private static final String VNP_AMOUNT_FIELD = "vnp_Amount";
    private static final String VNP_CURR_CODE_FIELD = "vnp_CurrCode";
    private static final String VNP_BANK_CODE_FIELD = "vnp_BankCode";
    private static final String VNP_TNX_REF_FIELD = "vnp_TxnRef";
    private static final String VNP_ORDER_INFO_FIELD = "vnp_OrderInfo";
    private static final String VNP_ORDER_TYPE_FIELD = "vnp_OrderType";
    private static final String VNP_LOCALE_FIELD = "vnp_Locale";
    private static final String VNP_RETURN_URL_FIELD = "vnp_ReturnUrl";
    private static final String VNP_IP_ADDR_FIELD = "vnp_IpAddr";
    private static final String VNP_CREATE_DATE_FIELD = "vnp_CreateDate";
    private static final String VNP_EXPIRE_DATE_FIELD = "vnp_ExpireDate";
    private static final String VNP_SECURE_HASH_FIELD = "vnp_SecureHash";
    private static final String VNP_SECURE_HASH_TYPE_FIELD = "vnp_SecureHashType";
    private static final String VNP_RESPONSE_CODE_FIELD = "vnp_ResponseCode";


    @SneakyThrows
    @Override
    public synchronized PaymentTransactionEntity doPay(OrderRequest order, HttpServletRequest req){
        long amount = order.getTotal().multiply(BigDecimal.valueOf(100)).intValue();
        String vnpRef = random(8);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put(VNP_VERSION_FIELD, properties.getVnpVersion());
        vnpParams.put(VNP_COMMAND_FIELD, "pay");
        vnpParams.put(VNP_TMN_CODE_FIELD, properties.getVnpTmnCode());
        vnpParams.put(VNP_AMOUNT_FIELD, String.valueOf(amount));
        vnpParams.put(VNP_CURR_CODE_FIELD, "VND");
        vnpParams.put(VNP_BANK_CODE_FIELD, "NCB");
        vnpParams.put(VNP_TNX_REF_FIELD, vnpRef);
        vnpParams.put(VNP_ORDER_INFO_FIELD, String.valueOf(order.getId()));
        vnpParams.put(VNP_ORDER_TYPE_FIELD, "other");

        String locate = req.getParameter("language");
        if (locate != null && !locate.isEmpty()) {
            vnpParams.put(VNP_LOCALE_FIELD, locate);
        } else {
            vnpParams.put(VNP_LOCALE_FIELD, "vn");
        }
        vnpParams.put(VNP_RETURN_URL_FIELD, properties.getVnpReturnUrl());
        vnpParams.put(VNP_IP_ADDR_FIELD, getIpAddress(req));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
        Date vnpCreateDate = cld.getTime();
        vnpParams.put(VNP_CREATE_DATE_FIELD, vnpDateFormat.format(vnpCreateDate));

        cld.add(Calendar.MINUTE, 15);
        Date expDate = cld.getTime();
        vnpParams.put(VNP_EXPIRE_DATE_FIELD, vnpDateFormat.format(expDate));

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if (isNotBlank(fieldValue)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnpSecureHash = hmacSHA512(properties.getVnpSecretKey(), hashData.toString());
        queryUrl += "&" + VNP_SECURE_HASH_FIELD + "=" + vnpSecureHash;

        PaymentTransactionEntity transaction = new PaymentTransactionEntity();
        transaction.setPayLink(properties.getVnpUrl() + "?" + queryUrl);
        transaction.setPaymentMethod(PaymentMethod.VNPAY);
        transaction.setAmount(Integer.parseInt(vnpParams.get(VNP_AMOUNT_FIELD)));
        transaction.setRef(vnpRef);
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setOrderInfo(vnpParams.get(VNP_ORDER_INFO_FIELD));
        transaction.setExpDate(expDate);
        transaction.setPayCreateDate(vnpCreateDate);
        transaction.setTimeZone(TIME_ZONE);
        return transaction;
    }

    private String hashAllFields(Map<String, String> fields) {
        String hashData = buildHashData(fields);
        return hmacSHA512(properties.getVnpSecretKey(), hashData);
    }

    @SneakyThrows
    @Override
    public synchronized void doIpn(Map<String, String> res){
        String vnpSecureHash = res.get(VNP_SECURE_HASH_FIELD);
        res.remove(VNP_SECURE_HASH_TYPE_FIELD);
        res.remove(VNP_SECURE_HASH_FIELD);
        String signValue = hashAllFields(res);

        if (signValue.equals(vnpSecureHash))
        {
            Optional<OrderEntity> orderEntityO = orderRepository.findById( Long.valueOf(res.get(VNP_ORDER_INFO_FIELD)));

            if (orderEntityO.isPresent()){
                var orderEntity = orderEntityO.get();
                Optional<PaymentTransactionEntity> transactionO = orderEntity
                        .getTransactions().stream()
                        .filter(transaction -> Objects.equals(transaction.getRef(), res.get(VNP_TNX_REF_FIELD))).findAny();
                transactionO.ifPresent(transaction ->{
                    boolean isValidAmountAndStatus = isValidAmountAndStatus(transaction, res);
                    populateIpnData(res, transaction);

                    if(isValidAmountAndStatus)
                    {
                        if ("00".equals(res.get(VNP_RESPONSE_CODE_FIELD)))
                        {
                            transaction.setStatus(PaymentStatus.SUCCESS);
                        } else {
                            transaction.setStatus(PaymentStatus.FAILED);
                        }
                    } else {
                        log.error("{\"RspCode\":\"04\",\"Message\":\"Invalid Amount\"}");
                        transaction.setStatus(PaymentStatus.FAILED);
                    }
                });
                orderRepository.save(orderEntity);
            }
            log.warn("Order Not found {}", res.get(VNP_ORDER_INFO_FIELD));
        }
        log.error("Invalid payload");
    }

    private void populateIpnData(Map<String, String> res, PaymentTransactionEntity transaction){
        transaction.setBankTranNo(res.get("vnp_BankTranNo"));
        transaction.setBankCode(res.get(VNP_BANK_CODE_FIELD));
        transaction.setResponseCode(res.get(VNP_RESPONSE_CODE_FIELD));
        transaction.setTransactionNo(res.get("vnp_TransactionNo"));
        transaction.setTransactionStatus(res.get("vnp_TransactionStatus"));
        try {
            Date payDate = vnpDateFormat.parse(res.get("vnp_PayDate"));
            transaction.setPayDate(payDate);
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    private boolean isValidAmountAndStatus(PaymentTransactionEntity transaction, Map<String, String> res) {
        boolean amountValid = false;
        boolean checkOrderStatus = true;

        if(Integer.parseInt(res.get(VNP_AMOUNT_FIELD)) == transaction.getAmount()){
            amountValid = true;
        }

        if (transaction.getStatus() != PaymentStatus.PENDING){
            checkOrderStatus = false;
        }
        return amountValid && checkOrderStatus;
    }
}
