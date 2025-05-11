package site.code4fun.service.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("all")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Member {
    private String id;
    private String email_address;
    private String unique_email_id;
    private String contact_id;
    private String full_name;
    private String web_id;
    private String email_type;
    private String status;
    private boolean consents_to_one_to_one_messaging;
    private String sms_phone_number;
    private String sms_subscription_status;
    private String sms_subscription_last_updated;
    private String ip_signup;
    private String timestamp_signup;
    private String ip_opt;
    private Date timestamp_opt;
    private int member_rating;
    private Date last_changed;
    private String language;
    private boolean vip;
    private String email_client;
    private String source;
    private MergeField merge_fields;

}