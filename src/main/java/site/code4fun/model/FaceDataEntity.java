package site.code4fun.model;

import jakarta.persistence.*;
import lombok.Data;
import site.code4fun.constant.AppConstants;
import site.code4fun.constant.ProcessingStatus;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "faces_data")
public class FaceDataEntity extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String content;

    @Enumerated(EnumType.STRING)
    private ProcessingStatus status;

    @Column(unique = true)
    private String providerId;
    private String fbId;
    private String srcUrl;

    @Column(length = 1000)
    private String thumbnail;

    @Column(length = 1000)
    private String avatar;
}