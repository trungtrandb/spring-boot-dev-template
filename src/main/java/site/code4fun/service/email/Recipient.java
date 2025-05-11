package site.code4fun.service.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class  Recipient {
    @JsonProperty("list_id")
    private String listId;
    @JsonProperty("list_is_active")
    private boolean listIsActive;
    @JsonProperty("list_name")
    private String listName;
    @JsonProperty("segment_text")
    private String segmentText;
    @JsonProperty("recipient_count")
    private int recipientCount;
}
