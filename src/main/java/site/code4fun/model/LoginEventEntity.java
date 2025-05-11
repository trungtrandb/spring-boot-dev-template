package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import site.code4fun.constant.AppConstants;
import java.time.LocalDateTime;

/**
 * Entity lưu trữ thông tin về các sự kiện đăng nhập của người dùng
 */
@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "login_event")
public class LoginEventEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "IP address không được để trống")
    @Column(nullable = false)
    private String ipAddress;

    private String location;

    private String deviceDetails;

    @Column(nullable = false)
    private LocalDateTime loginTime;

    @Column(nullable = false)
    private boolean loginSuccess;

    private String failureReason;
}