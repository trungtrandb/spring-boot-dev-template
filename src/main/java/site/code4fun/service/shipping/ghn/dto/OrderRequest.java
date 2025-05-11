package site.code4fun.service.shipping.ghn.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderRequest {
    @JsonProperty("payment_type_id")
    private Integer paymentTypeId;
    private String note;
    @JsonProperty("required_note")
    private String requiredNote; // CHOTHUHANG, CHOXEMHANGKHONGTHU, KHONGCHOXEMHANG

    @JsonProperty("from_name")
    private String fromName;

    @JsonProperty("from_phone")
    private String fromPhone;
    @JsonProperty("from_address")
    private String fromAddress;

    @JsonProperty("from_ward_name")
    private String fromWardName;

    @JsonProperty("from_district_name")
    private String fromDistrictName;

    @JsonProperty("from_province_name")
    private String fromProvinceName;

    @JsonProperty("return_phone")
    private String returnPhone;

    @JsonProperty("return_address")
    private String returnAddress;

    @JsonProperty("return_district_id")
    private Object returnDistrictId;

    @JsonProperty("return_ward_code")
    private String returnWardCode;

    @JsonProperty("client_order_code")
    private String clientOrderCode;

    @JsonProperty("to_name")
    private String toName;

    @JsonProperty("to_phone")
    private String toPhone;

    @JsonProperty("to_address")
    private String toAddress;

    @JsonProperty("to_ward_code")
    private String toWardCode;

    @JsonProperty("to_district_id")
    private Integer toDistrictId;

    @JsonProperty("cod_amount")
    private Integer codAmount;
    private String content;
    private Integer weight; //Maximum : 50.000gram
    private Integer length; // Maximum : 200cm
    private Integer width; // Maximum : 200cm
    private Integer height; // Maximum : 200cm

    @JsonProperty("pick_station_id")
    private Integer pickStationId;

    @JsonProperty("deliver_station_id")
    private Object deliverStationId;

    @JsonProperty("insurance_value")
    private Integer insuranceValue;

    @JsonProperty("service_id")
    private Integer serviceId;

    @JsonProperty("service_type_id")
    private Integer serviceTypeId;
    private Object coupon;

    @JsonProperty("pick_shift")
    private List<Integer> pickShift;
    private List<Item> items;
}
