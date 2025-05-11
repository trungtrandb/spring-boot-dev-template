package site.code4fun.service.payment;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import site.code4fun.constant.PaymentMethod;
import site.code4fun.constant.PaymentStatus;
import site.code4fun.model.OrderEntity;
import site.code4fun.model.PaymentTransactionEntity;
import site.code4fun.model.request.OrderRequest;
import site.code4fun.repository.jpa.OrderRepository;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static site.code4fun.constant.AppConstants.TIME_ZONE;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class BankTransfer implements PaymentProvider {
    private final OrderRepository orderRepository;
    private static final String LINK = "https://qr.sepay.vn/img?acc=0971972455&bank=MBBANK&amount=%s&des=%s";
    @Override
    public PaymentTransactionEntity doPay(OrderRequest order, HttpServletRequest req) {
        int amount = order.getTotal().multiply(BigDecimal.valueOf(100)).intValue();

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
        Date vnpCreateDate = cld.getTime();

        cld.add(Calendar.MINUTE, 15);
        Date expDate = cld.getTime();
        PaymentTransactionEntity transaction = new PaymentTransactionEntity();
        transaction.setPayLink(String.format(LINK,  order.getTotal(), order.getId()));
        transaction.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        transaction.setAmount(amount);
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setOrderInfo(String.valueOf(order.getId()));
        transaction.setExpDate(expDate);
        transaction.setPayCreateDate(vnpCreateDate);
        transaction.setTimeZone(TIME_ZONE);
        return transaction;
    }

    @Override
    public void doIpn(Map<String, String> res) {
        Optional<OrderEntity> orderEntityO = orderRepository.findById( Long.valueOf(res.get("content")));

        if (orderEntityO.isPresent()){
            var orderEntity = orderEntityO.get();
            Optional<PaymentTransactionEntity> transactionO = orderEntity
                    .getTransactions().stream()
                    .filter(transaction -> Objects.equals(transaction.getPaymentMethod(), PaymentMethod.BANK_TRANSFER)).findAny();
            transactionO.ifPresent(transaction ->{
                boolean isValidAmountAndStatus = isValidAmountAndStatus(transaction, res);
                populateIpnData(res, transaction);

                if(isValidAmountAndStatus)
                {
                    transaction.setStatus(PaymentStatus.SUCCESS);
                } else {
                    log.error("{\"RspCode\":\"04\",\"Message\":\"Invalid Amount\"}");
                    transaction.setStatus(PaymentStatus.FAILED);
                }
            });
            orderRepository.save(orderEntity);
        }
        log.warn("Order Not found {}", res.get("content"));
    }

        private void populateIpnData(Map<String, String> res, PaymentTransactionEntity transaction){
            transaction.setBankTranNo(res.get("accountNumber"));
            transaction.setBankCode(res.get("gateway"));
            transaction.setResponseCode(res.get("code"));
            transaction.setTransactionNo(res.get("id"));
            transaction.setRef("referenceCode");
            try {
                Date payDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(res.get("transactionDate"));
                transaction.setPayDate(payDate);
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }

        private boolean isValidAmountAndStatus(PaymentTransactionEntity transaction, Map<String, String> res) {
            boolean amountValid = false;
            boolean checkOrderStatus = true;

            if(Integer.parseInt(res.get("transferAmount")) == transaction.getAmount()){
                amountValid = true;
            }

            if (transaction.getStatus() != PaymentStatus.PENDING){
                checkOrderStatus = false;
            }
            return amountValid && checkOrderStatus;
        }
}
