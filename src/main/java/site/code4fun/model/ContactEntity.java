package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import site.code4fun.constant.AppConstants;

import java.time.LocalDate;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "contact")
public class ContactEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String providerId;

    @Column(length = 1000)
    private String avatar;

    @NotBlank
    @Size(min = 1, max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 200)
    @Column(length = 200)
    private String company;

    @Embedded
    private Address address;

    @Column(length = 20)
    private String phone;

    @Email
    @Size(max = 100)
    @Column(length = 100)
    private String email;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Size(max = 50)
    @Column(length = 50)
    private String source;
}