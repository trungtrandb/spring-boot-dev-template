package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import site.code4fun.constant.AppConstants;

/**
 * Entity class representing a product review
 */
@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "review")
public class ReviewEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 1, max = 1000, message = "Comment must be between 1 and 1000 characters")
    @Column(nullable = false)
    private String comment;

    @NotNull(message = "Order ID cannot be null")
    @Column(nullable = false)
    private Long orderId;

    @NotNull(message = "Product ID cannot be null")
    @Column(nullable = false)
    private Long productId;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be more than 5")
    @Column(nullable = false)
    private int rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;
}