package site.code4fun.model.dto;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Address {
    private String city;          // City name
    private String country;       // Country name
    private String province;  // Code representing the province
    private String provinceName;  // Name of the province
    private String district;  // Code representing the district
    private String districtName;  // Name of the district
    private String ward;      // Code representing the ward
    private String wardName;      // Name of the ward
    private String address;       // Street address or specific location

    private String zipCode;
    private String postalCode;

    private String fullName;
    private String phone;

}
