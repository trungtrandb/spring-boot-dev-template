package site.code4fun.service.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("all")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class MergeField {
    private String FNAME;
    private String LNAME;
    private String PHONE;
    private String BIRTHDAY;
    private Address ADDRESS;
}

