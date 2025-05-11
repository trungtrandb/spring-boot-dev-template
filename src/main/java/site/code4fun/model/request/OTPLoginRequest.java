package site.code4fun.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OTPLoginRequest {

    String code;
    String name;
    String email;
    @JsonProperty("otp_id")
    String otpId;
}
