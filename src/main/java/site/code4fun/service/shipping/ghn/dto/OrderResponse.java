package site.code4fun.service.shipping.ghn.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class OrderResponse implements Serializable {
    private String order_code;
    private String sort_code;
    private String trans_type;
    private String ward_encode;
    private String district_encode;
    private FeeResponse fee;
    private int total_fee;
    private Date expected_delivery_time;
    private String operation_partner;
}