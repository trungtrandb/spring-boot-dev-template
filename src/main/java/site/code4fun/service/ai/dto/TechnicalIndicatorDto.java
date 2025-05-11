package site.code4fun.service.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class TechnicalIndicatorDto implements Serializable {
//    private LocalDate date;
    private Long onBalanceVol;
    private Double closeValue;
    private Double simpMovingAvg;
    private Double exponentialMovingAvg;
    private Double rsi14;
    private Double atr14;
    private Double previousWeekClose;
    private Double dailyReturn;
    private Double weeklyReturn;
    private String bollingerBands;

}