package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import site.code4fun.constant.AppConstants;
import site.code4fun.constant.Status;
import site.code4fun.util.UrlParserUtils;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Entity class representing a category in the system.
 * Categories can be positioned in different areas of the application (header, left side, footer).
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = AppConstants.TABLE_PREFIX + "category")
public class CategoryEntity extends Auditable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 50, message = "Icon name cannot exceed 50 characters")
    private String icon;

    @NotBlank(message = "Slug is required")
    @Column(unique = true, nullable = false)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String content;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Enumerated(EnumType.STRING)
    private POSITION position;

    @OneToOne(
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private Attachment image;

    @PreUpdate
    @PrePersist
    private void preSave() {
        if (isEmpty(slug)) {
            setSlug(UrlParserUtils.buildPrettyURL(getName()));
        }
    }

    /**
     * Enum representing the possible positions where a category can be displayed
     */
    public enum POSITION {
        HEADER,
        LEFT_SIDE,
        FOOTER
    }
}