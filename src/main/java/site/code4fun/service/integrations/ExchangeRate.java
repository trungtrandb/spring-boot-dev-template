package site.code4fun.service.integrations;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("all")
public class ExchangeRate {

    public ResponseEntity<String> getRate(String baseCurrency, String targetCurrency) {
        return new RestTemplate().exchange("https://v6.exchangerate-api.com/v6/8a3782e4f5c0a2dbe9b9ddbf/latest/VND",  HttpMethod.GET, null, String.class);
    }
}
