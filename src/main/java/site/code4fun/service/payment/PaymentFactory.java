package site.code4fun.service.payment;

import lombok.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import site.code4fun.model.dto.PaymentMethodDTO;
import site.code4fun.model.dto.ShopConfigDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Lazy
public class PaymentFactory {
    private final Map<String, PaymentProvider> paymentProviders = new HashMap<>();
    public PaymentFactory(List<PaymentProvider> providers){
        providers.forEach(payment -> paymentProviders.put(payment.getClass().getSimpleName(), payment));
    }

    public PaymentProvider getProvider(@NonNull String provider) {
        for (Map.Entry<String, PaymentProvider> entry : paymentProviders.entrySet()){
            if (entry.getKey().toUpperCase().equalsIgnoreCase(provider)){
                return entry.getValue();
            }
        }
        return null;
    }

    public List<PaymentMethodDTO> getPaymentMethods(ShopConfigDTO shopConfig){ // TODO using admin config

        return paymentProviders.keySet().stream().map(method ->{
            PaymentMethodDTO dto = new PaymentMethodDTO();
            dto.setName(method);
            return dto;
        }).toList();
    }
}