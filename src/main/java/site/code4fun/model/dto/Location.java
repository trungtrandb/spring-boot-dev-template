package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class Location implements Serializable {
    public double lat;
    public double lng;
    public Object zip;
    public Object city;
    public String state;
    public String country;

    public String province;
    public String provinceName;
    public String district;
    public String districtName;
    public String ward;
    public String wardName;
    public String formattedAddress;
}