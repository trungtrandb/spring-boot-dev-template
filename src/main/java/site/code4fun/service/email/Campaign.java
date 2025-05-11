package site.code4fun.service.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Campaign{
    private String id;
    @JsonProperty("web_id")
    private String webId;
    @JsonProperty("create_time")
    private Date createTime;
    @JsonProperty("archive_url")
    private String archiveUrl;
    private String status;
    @JsonProperty("emails_sent")
    private int emailsSent;
    @JsonProperty("send_time")
    private String sendTime;
    @JsonProperty("content_type")
    private String contentType;
    @JsonProperty("needs_block_refresh")
    private boolean needsBlockRefresh;
    private boolean resendable;
    private Recipient recipients;
    private Settings settings;
}
