package site.code4fun.service.ai.dto;

import lombok.Data;

import java.util.List;

@Data
public class BidAsk {
//    private String code;
    private String symbol;
//    private String session;
    private List<PriceVolume> bidPrices;
//    private String receivedTime;
//    private String messageType;
    private List<PriceVolume> askPrices;
//    private String time;
}