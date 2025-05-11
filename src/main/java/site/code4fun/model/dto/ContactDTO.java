package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ContactDTO implements Serializable {
    private Long id;
    private String providerId;
    private String name;
    private String company;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String phone;
    private String email;
    private Date birthDay;
    private String source;
    private Date created;
}
