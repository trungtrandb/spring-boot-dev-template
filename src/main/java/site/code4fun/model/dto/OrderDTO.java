package site.code4fun.model.dto;

import lombok.Data;
import site.code4fun.constant.OrderStatus;
import site.code4fun.constant.PaymentStatus;
import site.code4fun.constant.ShippingMethod;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
public class OrderDTO implements Serializable {
    private Long id;
    private String name;
    private String email;
    private String note;
    private BigDecimal subTotal;
    private BigDecimal total;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal fees;
    private String paymentMethod;
    private PaymentStatus paymentStatus;
    private ShippingMethod shippingMethod;
    private String shippingStatus;
    private OrderStatus status; // 'PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'RETURNED', 'FAILED'
    private UserLite user;
    private int previousOrder;
    private Set<PaymentTransactionDTO> transactions = new HashSet<>();
    private UserLite updatedBy;
    private Date updated;
    private Address shippingAddress;
    private Date expectedDeliveryTime;

    private Set<OrderItemDTO> items = new HashSet<>();

}

