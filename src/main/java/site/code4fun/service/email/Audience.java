package site.code4fun.service.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@SuppressWarnings("all")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Audience {
    private String id;
    private String web_id;
    private String name;
    private Contact contact;
    private String permission_reminder;
    private boolean use_archive_bar;
    private CampaignDefault campaign_defaults;
    private String notify_on_subscribe;
    private String notify_on_unsubscribe;
    private Date date_created;
    private int list_rating;
    private boolean email_type_option;
    private String subscribe_url_short;
    private String subscribe_url_long;
    private String beamer_address;
    private String visibility;
    private boolean double_optin;
    private boolean has_welcome;
    private boolean marketing_permissions;
}


