package site.code4fun.service.shipping.ghn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class WardResponse implements Serializable {

    @JsonProperty("WardCode")
    private int wardCode;

    @JsonProperty("DistrictID")
    private int districtId;

    @JsonProperty("WardName")
    private String wardName;
}

