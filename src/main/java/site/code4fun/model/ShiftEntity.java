package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import site.code4fun.constant.AppConstants;
import site.code4fun.constant.DayOfMonth;
import site.code4fun.constant.DayOfWeek;
import site.code4fun.constant.Status;

import java.time.LocalTime;
import java.util.List;

/**
 * Entity class representing a work shift in the system.
 * Contains information about shift timing, working days, and break periods.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = AppConstants.TABLE_PREFIX + "shift")
public class ShiftEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Shift name is required")
    @Column(nullable = false)
    private String name = "Default Shift";

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @NotNull(message = "Start time is required")
    @Column(nullable = false)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(nullable = false)
    private LocalTime endTime;

    @NotNull(message = "Default status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceEntity.Status defaultStatusWhenNoCheckin = AttendanceEntity.Status.ABSENT;

    @ElementCollection
    @CollectionTable(name = AppConstants.TABLE_PREFIX + "shift_weekoffs", joinColumns = @JoinColumn(name = "id"))
    private List<DayOfWeek> weekOff;

    @ElementCollection
    @CollectionTable(name = AppConstants.TABLE_PREFIX + "shift_workingdays", joinColumns = @JoinColumn(name = "id"))
    private List<DayOfMonth> workingDays;

    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
}
