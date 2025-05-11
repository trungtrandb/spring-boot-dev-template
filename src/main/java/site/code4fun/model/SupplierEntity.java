package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import site.code4fun.constant.AppConstants;

/**
 * Entity class representing a supplier in the system.
 * Contains information about supplier details including contact information.
 */
@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "supplier")
public class SupplierEntity extends Auditable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Supplier name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    @Column(length = 15)
    private String phone;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    private Attachment image;
}