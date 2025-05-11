package site.code4fun.model;

import jakarta.persistence.*;
import lombok.Data;
import site.code4fun.constant.AppConstants;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "banner")
public class BannerEntity extends Auditable{

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private int typeId;
    private String description;

    @OneToOne(
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private Attachment image;
}