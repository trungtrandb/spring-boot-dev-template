package site.code4fun.service.shipping.ghn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class DistrictResponse implements Serializable {

    @JsonProperty("DistrictID")
    private int districtId;

    @JsonProperty("ProvinceID")
    private int provinceId;

    @JsonProperty("DistrictName")
    private String districtName;

    @JsonProperty("Code")
    private String code;

    @JsonProperty("Type")
    private int type;

    @JsonProperty("SupportType")
    private int supportType;
}

