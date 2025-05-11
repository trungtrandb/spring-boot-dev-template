package site.code4fun.model.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import site.code4fun.constant.OrderStatus;
import site.code4fun.constant.ShippingMethod;
import site.code4fun.model.dto.Address;
import site.code4fun.model.dto.PaymentTransactionDTO;
import site.code4fun.model.dto.ProductDTO;
import site.code4fun.model.dto.UserLite;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Data
public class OrderRequest implements Serializable {
    private Long id;
    private String note;
    private String address;
    @Getter(AccessLevel.NONE)
    private String name;
    private String email;
    private String firstName;
    private String lastName;
    private UserLite user;
    private Set<ProductDTO> items;
    private String paymentMethod;
    private ShippingMethod shippingMethod;
    private OrderStatus status;
    private Set<PaymentTransactionDTO> transactions = new HashSet<>();
    private BigDecimal total;
    private BigDecimal fees;
    private BigDecimal tax;
    private BigDecimal discount;
    private Address shippingAddress;

    public String getName(){
        return !isEmpty(name) ? name : String.format("%s %s", getLastName(), getFirstName());
    }
}

