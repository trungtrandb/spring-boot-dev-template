package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import site.code4fun.constant.AppConstants;

import java.io.Serializable;

/**
 * Entity class representing the content of a post.
 * Supports multi-language content with automatic translation capabilities.
 */
@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "post_content",
       indexes = {
           @Index(name = "idx_post_content_slug", columnList = "slug"),
           @Index(name = "idx_post_content_lang", columnList = "lang")
       })
@EqualsAndHashCode(exclude = {"post"})
@ToString(exclude = {"post"})
@EntityListeners(AuditingEntityListener.class)
public class PostContent implements Serializable {

    @Id
    @UuidGenerator
    private String id;

    @NotBlank(message = "Slug cannot be empty")
    @Size(max = 255, message = "Slug must be less than 255 characters")
    @Column(nullable = false, unique = true)
    private String slug;

    private boolean autoTranslate;

    @NotBlank(message = "Language code cannot be empty")
    @Size(min = 2, max = 2, message = "Language code must be exactly 2 characters")
    @Column(length = 2)
    private String lang;

    @Column(columnDefinition = "TEXT")
    private String content;

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Post post;
}
