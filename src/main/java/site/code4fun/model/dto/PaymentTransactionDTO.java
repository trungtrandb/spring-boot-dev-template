package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class PaymentTransactionDTO implements Serializable {
    private String payLink;
    private Date created;
    private String status;
}