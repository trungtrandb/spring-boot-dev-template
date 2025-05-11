package site.code4fun.model.dto;

import lombok.Data;

@Data
public class SendMailDTO {
    private String subject;
    private String content;
    private String emailAddress;
    private String phone;
}
