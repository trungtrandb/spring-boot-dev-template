package site.code4fun.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import site.code4fun.constant.AppConstants;
import site.code4fun.constant.PaymentMethod;
import site.code4fun.constant.PaymentStatus;

import java.util.Date;
@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "transaction")
public class PaymentTransactionEntity extends Auditable{

    @Id
    @UuidGenerator
    private String id;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // 'PENDING', 'SUCCESS', 'FAILED', 'REFUNDED', 'CANCELLED', 'EXPIRED'

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private String ref;
    private int amount;
    private String bankCode;
    private String bankTranNo;
    private String cardType;
    private Date payDate;
    private String orderInfo;
    private String transactionNo;
    private String responseCode;
    private String transactionStatus;
    @Column(length = 2000)
    private String payLink;
    private Date expDate;
    private String timeZone;
    private Date payCreateDate;
}