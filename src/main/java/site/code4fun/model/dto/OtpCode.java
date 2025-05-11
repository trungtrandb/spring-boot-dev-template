package site.code4fun.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class OtpCode implements Serializable {
    private String message;
    private boolean success;
    private String provider;
    private String id;
    private String phoneNumber;
    @JsonProperty("is_contact_exist")
    private boolean contactExist;
}