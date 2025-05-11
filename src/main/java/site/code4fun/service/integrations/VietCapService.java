package site.code4fun.service.integrations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import site.code4fun.constant.Period;
import site.code4fun.model.FinancialColumnEntity;
import site.code4fun.model.StockSymbolEntity;
import site.code4fun.model.dto.FinancialRatioResponse;
import site.code4fun.model.dto.TickerPricingHistoryDTO;
import site.code4fun.repository.jpa.FinancialColumnRepository;
import site.code4fun.repository.jpa.StockSymbolRepository;
import site.code4fun.service.ai.dto.StockDataResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static java.util.Collections.singletonList;
import static site.code4fun.constant.StockQuery.COLUMN_MAPPER_QUERY;
import static site.code4fun.constant.StockQuery.COMPANY_FINANCIAL_QUERY;

@Component
@Lazy
@Slf4j
@RequiredArgsConstructor
public class VietCapService {

    @Getter
    private final FinancialColumnRepository repository;
    private final StockSymbolRepository stockSymbolRepository;

    private final RestTemplate restTemplate = new RestTemplateBuilder().rootUri("https://trading.vietcap.com.vn/api").build();
    private static final RestClient restClient = RestClient.create("https://trading.vietcap.com.vn/data-mt/graphql");
    private static final HttpSyncGraphQlClient graphQlClient = HttpSyncGraphQlClient.builder(restClient)
            .header("Referer", "https://trading.vietcap.com.vn/")
            .header("Origin", "https://trading.vietcap.com.vn/")
            .header("DNT", "1")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36")
            .build();

    public List<FinancialColumnEntity> getFieldMapping() {
        if (getRepository().count() == 0) {
            List<FinancialColumnEntity> data = graphQlClient
                    .document(COLUMN_MAPPER_QUERY)
                    .retrieveSync("ListFinancialRatio")
                    .toEntityList(FinancialColumnEntity.class);
            return getRepository().saveAll(data);
        }
        return getRepository().findAll();
    }

    public List<Map<String, Object>> getFinancialReport(String ticker, Period period) {
        var res =  graphQlClient.document(COMPANY_FINANCIAL_QUERY)
                .variable("ticker", ticker)
                .variable("period", period)
                .retrieveSync("CompanyFinancialRatio")
                .toEntity(FinancialRatioResponse.class);
        return res != null ? res.getRatio() : new ArrayList<>();
    }

    public void pullUpdateAllSymbols() {
        List<StockSymbolEntity> lst = new ArrayList<>();
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(headers);
            ResponseEntity<List<StockSymbolEntity>> response = restTemplate.exchange(
                    "/price/symbols/getAll",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<>() { }
            );
            if (response.getBody() != null) {
                lst = response.getBody().stream().filter(item -> !item.getBoard().equalsIgnoreCase("DELISTED")).toList();
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        stockSymbolRepository.saveAll(lst);
    }

    public TickerPricingHistoryDTO getHistoryPrice(String ticker, LocalDate from, LocalDate to) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Referer", "https://trading.vietcap.com.vn/");
        headers.add("Origin", "https://trading.vietcap.com.vn/");
        long fromTimeStamp = from.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        long toTimeStamp = to.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("timeFrame", "ONE_DAY"); // ONE_MINUTE/ONE_HOUR
        requestBody.put("symbols", singletonList(ticker));
        requestBody.put("from", fromTimeStamp);
        requestBody.put("to", toTimeStamp);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        try {
            ResponseEntity<List<TickerPricingHistoryDTO>> response = restTemplate.exchange(
                    "/chart/OHLCChart/gap",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {}
            );
            return Objects.requireNonNull(response.getBody()).get(0);
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public StockDataResponse getLatestPrice(String ticker) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("symbols", singletonList(ticker));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<List<StockDataResponse>> response = restTemplate.exchange(
                "/price/symbols/getList",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() { }
        );
        return Objects.requireNonNull(response.getBody()).get(0);
    }
}
