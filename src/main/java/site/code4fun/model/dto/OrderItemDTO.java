package site.code4fun.model.dto;


import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderItemDTO implements Serializable {

    private String id;
    private String name;
    private String thumbnail;
    private String content;
    private BigDecimal price;
    private int quantity;
    private Long productId;
    private BigDecimal total;
}
