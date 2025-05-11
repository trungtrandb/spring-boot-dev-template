package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import site.code4fun.constant.AppConstants;
import site.code4fun.constant.Status;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity class representing a role in the system.
 * Roles are used to group privileges and assign them to users.
 */
@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "role")
public class Role extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Role name is required")
    @Size(min = 3, max = 50, message = "Role name must be between 3 and 50 characters")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Role title is required")
    @Size(min = 3, max = 100, message = "Role title must be between 3 and 100 characters")
    @Column(nullable = false)
    private String title;
    private boolean systemDefault;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @ElementCollection
    @CollectionTable(name = AppConstants.TABLE_PREFIX + "roles_privileges", 
        joinColumns = @JoinColumn(name = "id"))
    private Set<String> privileges = new HashSet<>();
}
