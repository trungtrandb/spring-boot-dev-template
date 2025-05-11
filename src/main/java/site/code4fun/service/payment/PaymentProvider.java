package site.code4fun.service.payment;

import jakarta.servlet.http.HttpServletRequest;
import site.code4fun.model.PaymentTransactionEntity;
import site.code4fun.model.request.OrderRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public interface PaymentProvider {
    PaymentTransactionEntity doPay(OrderRequest order, HttpServletRequest req);
    void doIpn(Map<String, String> res);

    default String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    default String buildHashData(Map<String, String> fields){
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (isNotBlank(fieldValue)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }
}
