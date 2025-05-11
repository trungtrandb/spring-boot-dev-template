package site.code4fun.service.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Settings {
    @JsonProperty("subject_line")
    private String subjectLine;
    private String title;
    @JsonProperty("from_name")
    private String fromName;
    @JsonProperty("reply_to")
    private String replyTo;
    @JsonProperty("use_conversation")
    private String useConversation;
    @JsonProperty("to_name")
    private String toName;
    @JsonProperty("folder_id")
    private String folderId;
    private boolean authenticate;
    @JsonProperty("auto_footer")
    private boolean autoFooter;
    @JsonProperty("inline_css")
    private boolean inlineCss;
    @JsonProperty("auto_tweet")
    private boolean autoTweet;
    @JsonProperty("fb_comments")
    private boolean fbComments;
    private boolean timewarp;
    @JsonProperty("template_id")
    private int templateId;
    @JsonProperty("drag_and_drop")
    private boolean dragAndDrop;
}
