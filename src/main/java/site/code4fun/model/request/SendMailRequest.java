package site.code4fun.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class SendMailRequest {
    private String subject;
    private String content;
    private List<Long> contacts;
    private List<Recipient> recipients;
    private String type;
    private SendTime sendTime;
    private Date scheduleExp;

    @Data
    public static class Recipient implements Serializable {
        private String email;
    }

    public enum SendTime {
        SCHEDULE, INSTANT
    }
}
