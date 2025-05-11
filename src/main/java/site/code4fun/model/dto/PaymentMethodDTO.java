package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PaymentMethodDTO implements Serializable {
    private String name;
    private String description;
    private Object content;
}
