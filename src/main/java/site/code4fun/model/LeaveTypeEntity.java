package site.code4fun.model;

import jakarta.persistence.*;
import lombok.Data;
import site.code4fun.constant.AppConstants;
import site.code4fun.constant.Status;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "leave_type")
public class LeaveTypeEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    private Double percentSalary;
    private Status status;
    @Column(length = 10)
    private String requestUnit;
    private int amount;
    private boolean createCalendar;
    private boolean requireApproved;
}
