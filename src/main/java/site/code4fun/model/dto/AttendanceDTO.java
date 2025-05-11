package site.code4fun.model.dto;

import lombok.Data;
import site.code4fun.model.AttendanceEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class AttendanceDTO implements Serializable {
    private Long id;
    private UserDTO user;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private long workedTime;
    private long overTime;
    private long breakTime;
    private AttendanceEntity.Status status;
    private Date created;
}
