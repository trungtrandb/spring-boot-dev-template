package site.code4fun.service.shipping.ghn.dto;

import lombok.Data;

@Data
public class ResponseDTO<T> {
    String code;
    String message;
    T data;
}
