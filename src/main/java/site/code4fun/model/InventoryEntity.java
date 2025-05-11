package site.code4fun.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import site.code4fun.constant.AppConstants;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "inventory")
public class InventoryEntity extends Auditable{

    @Id
    @UuidGenerator
    private String id;
    private String content;
    private int quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal cost;

    @ManyToOne
    private Product product;

    @ManyToOne
    private SupplierEntity supplier;

}
