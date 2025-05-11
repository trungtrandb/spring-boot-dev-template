package site.code4fun.model;


import jakarta.persistence.*;
import lombok.Data;
import site.code4fun.constant.*;
import site.code4fun.model.dto.Address;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "order")
public class OrderEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    @Column(length = 500)
    private String note;

    @Column(precision = 12, scale = 2)
    private BigDecimal subTotal;
    @Column(precision = 12, scale = 2)
    private BigDecimal total;
    @Column(precision = 4, scale = 2)
    private BigDecimal tax; // ex VAT = 0.1

    @Column(precision = 12, scale = 2)
    private BigDecimal fees; // ex delivery Fee = 1234.55
    @Column(precision = 12, scale = 2)
    private BigDecimal discount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private ShippingMethod shippingMethod;
    private String shippingStatus;
    private Date expectedDeliveryTime;

//    @Embedded
//    private Address billingAddress;
    @Embedded
    private Address shippingAddress;

    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PaymentTransactionEntity> transactions = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> items = new HashSet<>();

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public void addTransaction(PaymentTransactionEntity item) {
        transactions.add(item);
    }
}
