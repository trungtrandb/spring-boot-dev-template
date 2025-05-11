package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import site.code4fun.constant.AppConstants;
import site.code4fun.constant.Status;
import site.code4fun.util.DateTimeUtils;

import java.util.Date;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "leave_request")
public class LeaveRequestEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "From date is required")
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fromDate;

    @NotNull(message = "To date is required")
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date toDate;

    @Positive(message = "Duration in days must be positive")
    @Column(nullable = false)
    private int durationInDay;

    @Positive(message = "Duration in hours must be positive")
    @Column(nullable = false)
    private int durationInHour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @NotNull(message = "Leave type is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private LeaveTypeEntity leaveType;

    @PrePersist
    protected void onCreate() {
        setStatus(Status.PENDING);
        onUpdate();
    }

    @PreUpdate
    private void onUpdate() {
        if (fromDate != null && toDate != null) {
            if (fromDate.after(toDate)) {
                throw new IllegalArgumentException("From date cannot be after to date");
            }
            var diffInDay = (int) DateTimeUtils.dateDiff(fromDate, toDate) + 1;
            setDurationInDay(diffInDay);
            setDurationInHour(diffInDay * 8);
        }
    }
}
