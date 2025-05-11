package site.code4fun.service.shipping.ghn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ProvinceResponse implements Serializable {
    @JsonProperty("ProvinceID")
    public int provinceID;
    @JsonProperty("ProvinceName")
    public String provinceName;
    @JsonProperty("CountryID")
    public int countryID;
    @JsonProperty("Code")
    public String code;
    @JsonProperty("NameExtension")
    public List<String> nameExtension;
    @JsonProperty("IsEnable")
    public int isEnable;
    @JsonProperty("RegionID")
    public int regionID;
    @JsonProperty("RegionCPN")
    public int regionCPN;
    @JsonProperty("UpdatedBy")
    public int updatedBy;
    @JsonProperty("CreatedAt")
    public String createdAt;
    @JsonProperty("UpdatedAt")
    public String updatedAt;
    @JsonProperty("CanUpdateCOD")
    public boolean canUpdateCOD;
    @JsonProperty("Status")
    public int status;
}
