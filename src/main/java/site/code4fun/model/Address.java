package site.code4fun.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Embeddable
@Data
public class Address {
    @Size(max = 200)
    @Column(length = 200)
    private String address1;

    @Size(max = 200)
    @Column(length = 200)
    private String address2;

    @Size(max = 100)
    @Column(length = 100)
    private String city;

    @Size(max = 100)
    @Column(length = 100)
    private String state;

    @Size(max = 20)
    @Column(length = 20)
    private String zip;

    @Size(max = 100)
    @Column(length = 100)
    private String country;
} 