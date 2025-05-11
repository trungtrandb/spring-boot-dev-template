package site.code4fun.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
public class ChatRoomDTO implements Serializable {

    private Long id;
    private String name;
    @JsonProperty("isPrivate")
    private boolean isPrivate;
    @JsonProperty("isChatBot")
    private boolean isChatBot;
    @JsonProperty("isFinanceBot")
    private boolean isFinanceBot;
    private Set<UserLite> users = new HashSet<>();
    private MessageDTO lastMessage;
    private Date created;
    private UserLite createdBy;
    private String avatar;
}


