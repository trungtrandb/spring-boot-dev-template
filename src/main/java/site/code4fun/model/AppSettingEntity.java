package site.code4fun.model;

import jakarta.persistence.*;
import lombok.Data;
import site.code4fun.constant.AppConstants;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "settings")
public class AppSettingEntity extends Auditable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "config_key", length = 100)
    private String key;

    @Column(name = "config_value", length = 3000)
    private String value;
}