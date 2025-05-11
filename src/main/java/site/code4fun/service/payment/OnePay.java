package site.code4fun.service.payment;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import site.code4fun.ApplicationProperties;
import site.code4fun.model.PaymentTransactionEntity;
import site.code4fun.constant.PaymentMethod;
import site.code4fun.constant.PaymentStatus;
import site.code4fun.model.request.OrderRequest;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static site.code4fun.constant.AppConstants.TIME_ZONE;
import static site.code4fun.util.EncryptUtils.hmacSHA256;
import static site.code4fun.util.RandomUtils.random;

/**
 * - Card information Test
 *         Card Type    CardHolder Name    Card Number             Date(MM/YY)     OTP/CVV
 *         ABB - ATM    NGUYEN VAN A        9704250000000001        01/13           123456
 *         VCB - ATM    NGUYEN VAN A        9704360000000000002     01/13           123456
 *         Visa                             4000000000001091        05/2024         123
 *                                          4000000000001000
 *         Master 5123450000000008 05/2024 123
 *        //    private static final String  accessCode = "6BEB2546";
 * //    private static final String secretKey = "6D0870CDE5F24F34F3915FB0045120DB";
 * //    private static final String merchant = "TESTONEPAY";
 */
@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class OnePay implements PaymentProvider {

    private final ApplicationProperties properties;
    private static final String VPC_VERSION_FIELD = "vpc_Version";
    private static final String VPC_CURRENCY_FIELD = "vpc_Currency";
    private static final String VPC_COMMAND_FIELD = "vpc_Command";
    private static final String VPC_ACCESS_CODE_FIELD = "vpc_AccessCode";
    private static final String VPC_MERCHANT_FIELD = "vpc_Merchant";
    private static final String VPC_LOCALE_FIELD = "vpc_Locale";
    private static final String VPC_RETURN_URL_FIELD = "vpc_ReturnURL";
    private static final String VPC_MERCH_TXN_REF_FIELD = "vpc_MerchTxnRef";
    private static final String VPC_ORDER_INFO_FIELD = "vpc_OrderInfo";
    private static final String VPC_AMOUNT_FIELD = "vpc_Amount";
    private static final String VPC_TICKET_NO_FIELD = "vpc_TicketNo";
    private static final String VPC_SECURE_HASH_FIELD = "vpc_SecureHash";
    private static final String AGAIN_LINK_FIELD = "AgainLink";
    private static final String TITLE_FIELD = "Title";

    @Override
    public PaymentTransactionEntity doPay(OrderRequest order, HttpServletRequest req) {
        Map<String, String> reqFields = new HashMap<>();
        var vpcTxnRef= random(8);
        long amount = order.getTotal().multiply(BigDecimal.valueOf(100)).intValue();

        reqFields.put(VPC_VERSION_FIELD, "2");
        reqFields.put(VPC_CURRENCY_FIELD, "VND");
        reqFields.put(VPC_COMMAND_FIELD, "pay");
        reqFields.put(VPC_ACCESS_CODE_FIELD, properties.getAccessCode());
        reqFields.put(VPC_MERCHANT_FIELD, properties.getMerchant());
        reqFields.put(VPC_LOCALE_FIELD, "vn");
        reqFields.put(VPC_RETURN_URL_FIELD, properties.getReturnUrl());
        reqFields.put(VPC_MERCH_TXN_REF_FIELD, vpcTxnRef);
        reqFields.put(VPC_ORDER_INFO_FIELD, String.valueOf(order.getId()));
        reqFields.put(VPC_AMOUNT_FIELD, String.valueOf(amount));
        reqFields.put(VPC_TICKET_NO_FIELD, getIpAddress(req));
        reqFields.put(AGAIN_LINK_FIELD, "http://localhost:8888/again-link");
        reqFields.put(TITLE_FIELD, "Title field");

        List<String> fieldNames = new ArrayList<>(reqFields.keySet());
        Collections.sort(fieldNames);
        StringBuilder query = new StringBuilder("?");
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = reqFields.get(fieldName);
            if (isNotBlank(fieldValue)) {
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String secureHash = hashAllFields(reqFields);
        queryUrl += "&" + VPC_SECURE_HASH_FIELD +"=" + secureHash;

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
        Date createDate = cld.getTime();
        cld.add(Calendar.MINUTE, 60);
        Date expDate = cld.getTime();

        PaymentTransactionEntity transaction = new PaymentTransactionEntity();
        transaction.setPayLink(properties.getOnePayUrl() + queryUrl);
        transaction.setAmount(Integer.parseInt(reqFields.get(VPC_AMOUNT_FIELD)));
        transaction.setRef(vpcTxnRef);
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setOrderInfo(reqFields.get(VPC_ORDER_INFO_FIELD));
        transaction.setExpDate(expDate);
        transaction.setPayCreateDate(createDate);
        transaction.setTimeZone(TIME_ZONE);
        transaction.setPaymentMethod(PaymentMethod.ONEPAY);
        return transaction;
    }

    @Override
    public void doIpn(Map<String, String> res) {
        throw new NotImplementedException();
    }

    private String hashAllFields(Map<String, String> fields) {
        Map<String, String> vpcEntry = fields.entrySet().stream()
                .filter(entry -> entry.getKey().indexOf("vpc_") == 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<String> fieldNames = new ArrayList<>(vpcEntry.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vpcEntry.get(fieldName);
            if (isNotBlank(fieldValue)) {
                sb.append(fieldName).append("=").append(fieldValue);
                if (itr.hasNext()) {
                    sb.append('&');
                }
            }
        }
        return hmacSHA256(properties.getOnePaySecretKey(), sb.toString());
    }
}
