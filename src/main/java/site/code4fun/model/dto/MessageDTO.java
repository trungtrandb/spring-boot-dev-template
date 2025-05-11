package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class MessageDTO implements Serializable {
    private Long id;
    private String content;
    private UserLite createdBy;
    private Room room;
    private Date created;

    @Data
    public static class Room implements Serializable{
        private Long id;
    }


}
