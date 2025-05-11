package site.code4fun.model;

import jakarta.persistence.*;
import lombok.Data;
import site.code4fun.constant.*;
import site.code4fun.util.UrlParserUtils;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "layout_type")
public class LayoutTypeEntity extends Auditable{

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String slug;
    @Column(length = 5)
    private String language;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String icon;

    @OneToMany( cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BannerEntity> banners = new ArrayList<>();

    @OneToMany( cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> sliders = new ArrayList<>();

    @OneToMany( cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> promotionSliders = new ArrayList<>();

    boolean home;
    @Enumerated(EnumType.STRING)
    private LayoutType layoutType;

    @Enumerated(EnumType.STRING)
    private ProductCardLayout productCard;

    @PreUpdate
    @PrePersist
    private void preSave(){
        setSlug(UrlParserUtils.buildPrettyURL(getName()));
    }

}