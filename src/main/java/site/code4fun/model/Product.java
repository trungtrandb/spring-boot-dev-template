package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import site.code4fun.constant.AppConstants;
import site.code4fun.constant.Status;
import site.code4fun.model.dto.ProductSize;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "product")
@SuppressWarnings("unused")
@EqualsAndHashCode(exclude = {"tags", "categories", "files", "inventories"})
public class Product extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 500)
    @Column(length = 500)
    private String thumbnail;

    @NotBlank
    @Size(max = 100)
    private String sku;

    @NotBlank
    @Size(max = 255)
    @Column(unique = true)
    private String slug;

    @Size(max = 4000)
    @Column(length = 4000)
    private String content;

    @NotNull
    @Positive
    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    private BigDecimal discount;

    @Positive
    private int quantity;

    @Size(max = 50)
    private String unit;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(
        name = AppConstants.TABLE_PREFIX + "products_categories",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<CategoryEntity> categories = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(
        name = AppConstants.TABLE_PREFIX + "products_tags",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tags = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = AppConstants.TABLE_PREFIX + "products_attachments",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "attachment_id")
    )
    private Set<Attachment> files = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "product", fetch = FetchType.LAZY)
    private Set<InventoryEntity> inventories = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private SupplierEntity supplier;

    @Embedded
    private ProductSize productSize;

    public BigDecimal getCost() {
        return inventories.stream()
                .map(InventoryEntity::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getFinalPrice() {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }
        return price.subtract(discount);
    }

    public void addFile(Attachment file) {
        if (file != null) {
            this.files.add(file);
        }
    }

    public void removeFile(Attachment file) {
        if (file != null) {
            this.files.remove(file);
        }
    }

    public void addCategory(CategoryEntity category) {
        if (category != null) {
            categories.add(category);
        }
    }

    public void removeCategory(CategoryEntity category) {
        if (category != null) {
            categories.remove(category);
        }
    }

    public void addTag(TagEntity tag) {
        if (tag != null) {
            tags.add(tag);
            tag.getProducts().add(this);
        }
    }

    public void removeTag(TagEntity tag) {
        if (tag != null) {
            tags.remove(tag);
            tag.getProducts().remove(this);
        }
    }

    public void addInventoryEntity(InventoryEntity item) {
        if (item != null) {
            item.setProduct(this);
            inventories.add(item);
        }
    }

    public void removeInventoryEntity(InventoryEntity item) {
        if (item != null) {
            inventories.remove(item);
            item.setProduct(null);
        }
    }

    public boolean hasStock() {
        return quantity > 0;
    }

    public boolean isDiscounted() {
        return discount != null && discount.compareTo(BigDecimal.ZERO) > 0;
    }
}