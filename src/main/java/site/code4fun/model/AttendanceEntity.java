package site.code4fun.model;


import jakarta.persistence.*;
import lombok.Data;
import site.code4fun.constant.AppConstants;

import java.time.LocalDateTime;

/**
 * Entity class representing attendance records
 */
@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "attendance", indexes = {
    @Index(name = "idx_attendance_user", columnList = "user_id"),
    @Index(name = "idx_attendance_check_in", columnList = "check_in")
})
public class AttendanceEntity extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "check_in")
    private LocalDateTime checkIn;

    @Column(name = "check_out")
    private LocalDateTime checkOut;

    private long workedTime;
    private long overTime;
    private long breakTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        ABSENT, PRESENT, HOLIDAY
    }
}

