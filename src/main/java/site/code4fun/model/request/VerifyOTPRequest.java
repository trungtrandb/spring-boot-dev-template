package site.code4fun.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VerifyOTPRequest{

     private String code;
     @JsonProperty("otp_id")
     private String otpId;
 }