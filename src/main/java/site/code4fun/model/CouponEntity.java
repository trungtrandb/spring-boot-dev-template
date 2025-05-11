package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import site.code4fun.constant.AppConstants;
import site.code4fun.constant.Status;

import java.util.Date;

/**
 * Entity class representing a coupon in the system
 */
@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "coupon")
public class CouponEntity extends Auditable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Coupon name is required")
    @Size(max = 100, message = "Coupon name must be less than 100 characters")
    private String name;

    @NotBlank(message = "Coupon code is required")
    @Size(max = 50, message = "Coupon code must be less than 50 characters")
    @Column(unique = true)
    private String code;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @NotBlank(message = "Coupon type is required")
    @Size(max = 50, message = "Coupon type must be less than 50 characters")
    private String type;

    @Min(value = 0, message = "Amount cannot be negative")
    private Integer amount;

    @Min(value = 0, message = "Minimum cart amount cannot be negative")
    private Integer minimumCartAmount;

    @NotNull(message = "Active from date is required")
    @Temporal(TemporalType.TIMESTAMP)
    private Date activeFrom;

    @NotNull(message = "Expiry date is required")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expireAt;

    @Min(value = 0, message = "Target cannot be negative")
    private int target;

    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Attachment image;

    @ManyToOne(fetch = FetchType.LAZY)
    private User userId;
}