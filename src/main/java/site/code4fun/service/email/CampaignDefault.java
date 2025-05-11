package site.code4fun.service.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CampaignDefault{
    @JsonProperty("from_name")
    private String fromName;
    @JsonProperty("from_email")
    private String fromEmail;
    private String subject;
    private String language;
}