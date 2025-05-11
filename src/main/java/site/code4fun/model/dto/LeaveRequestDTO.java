package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class LeaveRequestDTO implements Serializable {
    private Long id;
    private String name;
    private Date fromDate;
    private Date toDate;
    private int durationInDay;
    private int durationInHour;
    private LeaveTypeDTO leaveType;
    private UserDTO createdBy;
    private UserDTO updatedBy;
    private String status;
}
