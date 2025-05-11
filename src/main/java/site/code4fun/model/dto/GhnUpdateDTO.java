package site.code4fun.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GhnUpdateDTO { // Ghn hook when order status change

    @JsonProperty("CODAmount")
    private long codAmount;

    @JsonProperty("CODTransferDate")
    private String codTransferDate; // Assuming this can be null, use String or LocalDateTime based on your needs

    @JsonProperty("ClientOrderCode")
    private String clientOrderCode;

    @JsonProperty("ConvertedWeight")
    private int convertedWeight;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Fee")
    private Fee fee;

    @JsonProperty("Height")
    private int height;

    @JsonProperty("IsPartialReturn")
    private boolean isPartialReturn;

    @JsonProperty("Length")
    private int length;

    @JsonProperty("OrderCode")
    private String orderCode;

    @JsonProperty("PartialReturnCode")
    private String partialReturnCode;

    @JsonProperty("PaymentType")
    private int paymentType;

    @JsonProperty("Reason")
    private String reason;

    @JsonProperty("ReasonCode")
    private String reasonCode;

    @JsonProperty("ShopID")
    private long shopId;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Time")
    private String time; // Use LocalDateTime if you want to handle time properly

    @JsonProperty("TotalFee")
    private long totalFee;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Warehouse")
    private String warehouse;

    @JsonProperty("Weight")
    private int weight;

    @JsonProperty("Width")
    private int width;


    @Data
    public static class Fee {

        @JsonProperty("CODFailedFee")
        private long codFailedFee;

        @JsonProperty("CODFee")
        private long codFee;

        @JsonProperty("Coupon")
        private long coupon;

        @JsonProperty("DeliverRemoteAreasFee")
        private long deliverRemoteAreasFee;

        @JsonProperty("DocumentReturn")
        private long documentReturn;

        @JsonProperty("DoubleCheck")
        private long doubleCheck;

        @JsonProperty("Insurance")
        private long insurance;

        @JsonProperty("MainService")
        private long mainService;

        @JsonProperty("PickRemoteAreasFee")
        private long pickRemoteAreasFee;

        @JsonProperty("R2S")
        private long r2s;

        @JsonProperty("Return")
        private long returnFee; // Renamed to avoid conflict with reserved keyword

        @JsonProperty("StationDO")
        private long stationDO;

        @JsonProperty("StationPU")
        private long stationPU;

        @JsonProperty("Total")
        private long total;
    }
}
