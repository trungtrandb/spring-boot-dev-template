package site.code4fun.service.shipping.ghn.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class Item implements Serializable {
    private String name;
    private int quantity;
    private int height;
    private int weight;
    private int length;
    private int width;
    private String code;
    private int price;
}