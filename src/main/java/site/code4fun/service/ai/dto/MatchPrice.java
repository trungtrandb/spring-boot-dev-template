package site.code4fun.service.ai.dto;

import lombok.Data;

@Data
public class MatchPrice {
//    private String code;
    private String symbol;
    private int matchPrice;
    private int matchVol;
//    private String receivedTime;
//    private String messageType;
    private int accumulatedVolume;
    private double accumulatedValue;
    private double avgMatchPrice;
    private int highest;
    private int lowest;
    private String time;
//    private String session;
//    private String matchType;
    private int foreignSellVolume;
    private int foreignBuyVolume;
//    private int currentRoom;
//    private long totalRoom;
    private double totalAccumulatedValue;
    private int totalAccumulatedVolume;
    private int referencePrice;
}