package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import site.code4fun.constant.AppConstants;
import site.code4fun.util.UrlParserUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity class representing a tag in the system.
 * Tags are used to categorize products and can be associated with multiple products.
 */
@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "tag")
public class TagEntity extends Auditable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tag name cannot be empty")
    @Size(min = 2, max = 50, message = "Tag name must be between 2 and 50 characters")
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @ManyToMany(mappedBy = "tags")
    private Set<Product> products = new HashSet<>();

    /**
     * Pre-save hook to generate a unique slug from the tag name.
     * The slug is created by combining the tag name with a timestamp to ensure uniqueness.
     */
    @PreUpdate
    @PrePersist
    private void preSave() {
        if (name != null && !name.isEmpty()) {
            String baseSlug = UrlParserUtils.buildPrettyURL(name);
            setSlug(baseSlug + "-" + System.currentTimeMillis());
        }
    }
}