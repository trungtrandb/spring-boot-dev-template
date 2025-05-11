package site.code4fun.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.code4fun.constant.Language;
import site.code4fun.constant.Period;
import site.code4fun.constant.ReportType;
import site.code4fun.model.FinancialColumnEntity;
import site.code4fun.repository.jpa.FinancialColumnRepository;
import site.code4fun.service.ai.dto.MatchPrice;
import site.code4fun.service.integrations.VietCapService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class StockService {

    private final FinancialColumnRepository repository;
    private final VietCapService vciRest;

    private Map<String, Object> mapColumn(List<FinancialColumnEntity> lstCol, Map<String, Object> report, Language language ) {
        Map<String, FinancialColumnEntity> mapCols = lstCol.stream()
                .collect(Collectors.toMap(
                        FinancialColumnEntity::getFieldName, Function.identity(),
                        (existing, replacement) -> existing)
                );

        Map<String, Object> newMap = new HashMap<>();
        report.keySet().forEach(key -> {
            if ( mapCols.get(key) != null){
                FinancialColumnEntity columnEntity = mapCols.get(key);
                newMap.put(language == Language.EN ? columnEntity.getEnName() : columnEntity.getName(), report.get(key));
            }else if (List.of("ticker", "yearReport", "symbol").contains(key)){
                newMap.put(key, report.get(key));
            }
        });
        return newMap;
    }

    public List<Map<String, Object>> getReportByYear(String ticker, Period period, Integer year, Language language) {
        List<FinancialColumnEntity> blCol = getColumnsByType(ReportType.BalanceSheet);
        List<FinancialColumnEntity> plCol = getColumnsByType(ReportType.ProfitAndLoss);
        List<FinancialColumnEntity> mergeList = new ArrayList<>();
        mergeList.addAll(blCol);
        mergeList.addAll(plCol);

        var lst = getRawReportByTypeAndYear(ticker, period, year);
        List<Map<String, Object>> res = new ArrayList<>();
        for (Map<String, Object> map : lst) {
            res.add(mapColumn(mergeList, map, language));
        }
        return res;
    }

    public List<Map<String,Object>> getRawReportByTypeAndYear(String ticker, Period period, Integer year) {
        List<Map<String, Object>> metrics = vciRest.getFinancialReport(ticker, period);

        return year == null ? metrics : metrics.stream()
                .filter(map ->  map.get("yearReport").toString().contains(String.valueOf(year)) )
                .toList();
    }

    private List<FinancialColumnEntity> getColumnsByType(ReportType reportType){
        List<FinancialColumnEntity> lstCol = vciRest.getFieldMapping();
        return lstCol.stream()
                .filter(col-> col.getType().equalsIgnoreCase(reportType.getValue()))
                .collect(Collectors.toMap(
                        FinancialColumnEntity::getFieldName, Function.identity(),
                        (existing, replacement) -> existing)
                ).values().stream().toList();
    }

    public MatchPrice getLatestPrice(String ticker){
        var res = vciRest.getLatestPrice(ticker);
        return res.getMatchPrice();
    }
}
