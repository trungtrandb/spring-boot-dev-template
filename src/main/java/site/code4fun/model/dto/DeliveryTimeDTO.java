package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeliveryTimeDTO implements Serializable {
    private String title;
    private String description;
}
