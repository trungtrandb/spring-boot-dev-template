package site.code4fun.service.ai.dto;

import lombok.Data;

@Data
public class ListingInfo {
        private String code;
        private String symbol;
        private int ceiling;
        private int floor;
        private int refPrice;
//        private String stockType;
//        private String board;
        private int exercisePrice;
        private String exerciseRatio;
        private String maturityDate;
        private String lastTradingDate;
        private String underlyingSymbol;
        private String issuerName;
        private long listedShare;
        private String receivedTime;
        private String messageType;
        private String type;
        private long id;
//        private String enOrganName;
        private String enOrganShortName;
//        private String organName;
        private String organShortName;
        private String ticker;
        private int priorClosePrice;
        private String benefit;
        private String tradingDate;

    }