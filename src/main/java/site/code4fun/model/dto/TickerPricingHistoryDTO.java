package site.code4fun.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class  TickerPricingHistoryDTO {
    private String symbol;

    @JsonProperty("o")
    private List<Double> open;

    @JsonProperty("h")
    private List<Double> high;

    @JsonProperty("l")
    private List<Double> low;

    @JsonProperty("c")
    private List<Double> close;

    @JsonProperty("v")
    private List<Long> volume;

    @JsonProperty("t")
    private List<Long> time;
}
