package site.code4fun.model;

import jakarta.persistence.*;
import lombok.Data;
import site.code4fun.constant.AppConstants;

import java.util.Date;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "campaign")
public class CampaignEntity extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}
