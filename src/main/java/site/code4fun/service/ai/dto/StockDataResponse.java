package site.code4fun.service.ai.dto;

import lombok.Data;

@Data
public class StockDataResponse {
//    private ListingInfo listingInfo;
    private BidAsk bidAsk;
    private MatchPrice matchPrice;
}