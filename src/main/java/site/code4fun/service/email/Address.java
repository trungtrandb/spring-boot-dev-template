package site.code4fun.service.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@SuppressWarnings("all")
public class Address {
    private String addr1;
    private String addr2;
    private String city;
    private String state;
    private String zip;
    private String country;
}
