package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import site.code4fun.constant.AppConstants;
import site.code4fun.model.dto.ProductSize;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entity đại diện cho một mục trong đơn hàng
 */
@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "order_item")
public class OrderItem implements Serializable {
    @Id
    @UuidGenerator
    private String id;

    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    @Column(length = 1000)
    private String thumbnail;

    @NotNull(message = "Tên sản phẩm không được để trống")
    @Column(nullable = false)
    private String name;

    @Embedded
    private ProductSize productSize;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal total;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    private BigDecimal discount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
}
