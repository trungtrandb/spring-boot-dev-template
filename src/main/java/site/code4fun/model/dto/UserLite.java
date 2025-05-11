package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLite implements Serializable {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String title;
    private String avatar;
    private String address;
    private String gender;
    private String langKey;
}