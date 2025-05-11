package site.code4fun.service.shipping.ghn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class FeeResponse implements Serializable {
    private int main_service;
    private int insurance;
    private int cod_fee;
    private int station_do;
    private int station_pu;
    @JsonProperty("return")
    private int ret;
    private int r2s;
    private int return_again;
    private int coupon;
    private int document_return;
    private int double_check;
    private int double_check_deliver;
    private int pick_remote_areas_fee;
    private int deliver_remote_areas_fee;
    private int pick_remote_areas_fee_return;
    private int deliver_remote_areas_fee_return;
    private int cod_failed_fee;
}