package site.code4fun.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import site.code4fun.constant.AppConstants;
import site.code4fun.constant.PostType;
import site.code4fun.constant.Status;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity class representing a Post in the system.
 * Contains information about the post's content, status, and relationships.
 */
@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "post")
public class Post extends Auditable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10) default 'DRAFT'", nullable = false)
    private Status status;
    private String ref;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) default 'POST'", nullable = false)
    private PostType type;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<PostContent> postContents = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Attachment banner;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Attachment attachment;
}