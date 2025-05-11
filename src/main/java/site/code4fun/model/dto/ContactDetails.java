package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ContactDetails implements Serializable {
    private String contact; // phoneNumber
    private List<Social> socials;
    private String website;
    private String emailAddress;
    private Location location;
}