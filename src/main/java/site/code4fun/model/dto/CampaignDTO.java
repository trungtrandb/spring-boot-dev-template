package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CampaignDTO implements Serializable {
    private Long id;
    private String providerId;
    private String name;
    private String title;
    private String status;
    private int emailsSent;
    private Date sendTime;
    private String contentType;
    private String fromName;
    private String replyTo;
    private String useConversation;
    private String toName;
    private int templateId;
    private Date created;
}
