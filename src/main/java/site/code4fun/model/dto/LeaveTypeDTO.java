package site.code4fun.model.dto;

import lombok.Data;
import site.code4fun.constant.Status;

import java.io.Serializable;

@Data
public class LeaveTypeDTO implements Serializable {
    private Long id;
    private String name;

    private Double percentSalary;
    private Status status;
    private String requestUnit;
    private int amount;
    private boolean createCalendar;
    private boolean requireApproved;
}
