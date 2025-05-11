package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import site.code4fun.constant.AppConstants;
import site.code4fun.constant.Status;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity class representing an attachment in the system.
 * This class manages file attachments with their metadata and relationships.
 */
@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "attachment")
public class Attachment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @Size(max = 500, message = "Link must be less than 500 characters")
    @Column(length = 500)
    private String link;

    @Size(max = 500, message = "Thumbnail must be less than 500 characters")
    @Column(length = 500)
    private String thumbnail;

    @Size(max = 100, message = "Author must be less than 100 characters")
    private String author;

    @Size(max = 3000, message = "Content must be less than 3000 characters")
    @Column(length = 3000)
    private String content;

    @Size(max = 100, message = "Content type must be less than 100 characters")
    private String contentType;

    private Long size;
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    private String folderPath;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = AppConstants.TABLE_PREFIX + "users_documents",
        joinColumns = @JoinColumn(name = "attachment_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();

    @ManyToMany(mappedBy = "files", fetch = FetchType.LAZY)
    @Setter(AccessLevel.NONE)
    private Set<Product> products = new HashSet<>();

    @Size(max = 100, message = "Storage provider must be less than 100 characters")
    private String storageProvider;

    @Size(max = 100, message = "Storage ID must be less than 100 characters")
    private String storageId;

    private Date expired;

    /**
     * Adds a user to the attachment's users collection
     * @param user the user to add
     */
    public void addUser(User user) {
        if (user != null) {
            users.add(user);
        }
    }

    /**
     * Removes a user from the attachment's users collection
     * @param user the user to remove
     */
    public void removeUser(User user) {
        if (user != null) {
            users.remove(user);
        }
    }

    /**
     * Sets the users collection for this attachment
     * @param users the collection of users to set
     */
    public void setUsers(Collection<User> users) {
        if (users != null) {
            this.users.clear();
            this.users.addAll(users);
        }
    }

    /**
     * Checks if the attachment has expired
     * @return true if the attachment has expired, false otherwise
     */
    public boolean isExpired() {
        return expired != null && expired.before(new Date());
    }

    /**
     * Gets the file extension from the name
     * @return the file extension or null if no extension exists
     */
    public String getFileExtension() {
        if (name == null || !name.contains(".")) {
            return null;
        }
        return name.substring(name.lastIndexOf(".") + 1);
    }
}

