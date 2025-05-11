package site.code4fun.service.payment;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import site.code4fun.constant.PaymentMethod;
import site.code4fun.constant.PaymentStatus;
import site.code4fun.model.PaymentTransactionEntity;
import site.code4fun.model.request.OrderRequest;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import static site.code4fun.constant.AppConstants.TIME_ZONE;

@Service
@Lazy
public class Cod implements PaymentProvider{
    @Override
    public PaymentTransactionEntity doPay(OrderRequest order, HttpServletRequest req) {
        int amount = order.getTotal().multiply(BigDecimal.valueOf(100)).intValue();

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
        Date vnpCreateDate = cld.getTime();
        PaymentTransactionEntity transaction = new PaymentTransactionEntity();
        transaction.setPaymentMethod(PaymentMethod.COD);
        transaction.setAmount(amount);
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setOrderInfo(String.valueOf(order.getId()));
        transaction.setPayCreateDate(vnpCreateDate);
        transaction.setTimeZone(TIME_ZONE);
        return transaction;
    }

    @Override
    public void doIpn(Map<String, String> res) {
        // TODO
    }
}
