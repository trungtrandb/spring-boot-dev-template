package site.code4fun.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class CouponDTO implements Serializable {
    private int id;
    private String code;
    private String language;
    private String description;
    private AttachmentDTO image;
    private String type;
    private int amount;
    @JsonProperty("minimum_cart_amount")
    private int minimumCartAmount;
    @JsonProperty("active_from")
    private Date activeFrom;
    @JsonProperty("expire_at")
    private Date expireAt;
    @JsonProperty("is_valid")
    private boolean isValid;
    private int target;
    @JsonProperty("is_approve")
    private int isApprove;
    @JsonProperty("translated_languages")
    private List<String> translatedLanguages;

    @JsonProperty("shop_id")
    private int shopId;
    @JsonProperty("user_id")
    private UserDTO userId;
}