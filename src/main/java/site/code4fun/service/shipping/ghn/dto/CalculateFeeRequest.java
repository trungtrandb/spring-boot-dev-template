package site.code4fun.service.shipping.ghn.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalculateFeeRequest implements Serializable {
    @JsonProperty("service_type_id")
    private Integer serviceTypeId;
    @JsonProperty("to_district_id")
    private Integer toDistrictId;
    @JsonProperty("to_ward_code")
    private String toWardCode;
    private Integer weight;
}
