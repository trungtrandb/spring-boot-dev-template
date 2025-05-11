package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import site.code4fun.constant.AppConstants;

/**
 * Entity class representing OTP (One-Time Password) codes in the system.
 * This entity stores information about OTP codes sent to users via phone or email.
 */
@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "otp_code")
public class OtpCodeEntity extends Auditable {

    @Id
    @UuidGenerator
    private String id;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String code;

    @Column(length = 20)
    private String phoneNumber;

    @Email
    @Column(length = 100)
    private String email;

    private String message;

    @Column(nullable = false)
    private boolean success;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String provider;

    @Column(length = 100)
    private String messageId;
}