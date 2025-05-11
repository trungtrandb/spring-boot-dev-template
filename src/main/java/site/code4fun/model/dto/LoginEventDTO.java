package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class LoginEventDTO implements Serializable {
    private Long id;
    private String ipAddress;
    private String location;
    private String deviceDetails;
    private Date created;
}
