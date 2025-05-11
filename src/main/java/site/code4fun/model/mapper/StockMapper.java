package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.dto.TickerPricingHistoryDTO;
import site.code4fun.service.ai.dto.PriceData;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface StockMapper {
    default List<PriceData> mapToPriceDataList(TickerPricingHistoryDTO dto) {
        if (dto == null || dto.getTime() == null || dto.getClose() == null ||
                dto.getOpen() == null || dto.getHigh() == null || dto.getLow() == null ||
                dto.getVolume() == null) {
            System.err.println("Cảnh báo: DTO hoặc một trong các danh sách dữ liệu cơ bản là null.");
            return Collections.emptyList();
        }

        List<Long> times = dto.getTime();
        List<Double> opens = dto.getOpen();
        List<Double> highs = dto.getHigh();
        List<Double> lows = dto.getLow();
        List<Double> closes = dto.getClose();
        List<Long> volumes = dto.getVolume();

        int size = times.size();
        if (opens.size() != size || highs.size() != size || lows.size() != size ||
                closes.size() != size || volumes.size() != size) {
            System.err.println("Cảnh báo: Các danh sách trong DTO không có cùng kích thước. Dữ liệu có thể không nhất quán.");
            size = Math.min(size, opens.size());
            size = Math.min(size, highs.size());
            size = Math.min(size, lows.size());
            size = Math.min(size, closes.size());
            size = Math.min(size, volumes.size());
            System.err.println("Chỉ xử lý " + size + " phần tử đầu tiên.");
        }

        if (size == 0) {
            return Collections.emptyList();
        }

        List<PriceData> priceDataList = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            try {
                long timestampValue = times.get(i);
                LocalDate dateTimeUTC = LocalDate.ofInstant(
                        Instant.ofEpochSecond(timestampValue),
                        ZoneId.systemDefault()
                );

                double open = opens.get(i);
                double high = highs.get(i);
                double low = lows.get(i);
                double close = closes.get(i);
                long volume = volumes.get(i);

                priceDataList.add(new PriceData(dateTimeUTC, open, high, low, close, volume));

            } catch (Exception e) {
                System.err.println("Lỗi khi xử lý dữ liệu tại chỉ số " + i + ": " + e.getMessage());
            }
        }

         priceDataList.sort(Comparator.comparing(PriceData::date));
        return priceDataList;
    }


}
