package site.code4fun.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;

/**
 * Base class for all auditable entities.
 * Provides automatic auditing of creation and modification timestamps and users.
 */
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"createdBy", "updatedBy"})
@ToString(exclude = {"createdBy", "updatedBy"})
public abstract class Auditable implements Serializable {

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false)
    @JsonIgnore
    private User createdBy;

    @LastModifiedDate
    @NotNull
    private Date updated;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User updatedBy;

    @CreatedDate
    @Column(updatable = false)
    @NotNull
    private Date created;
}
