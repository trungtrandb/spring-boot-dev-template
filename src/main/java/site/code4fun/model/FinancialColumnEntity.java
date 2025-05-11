package site.code4fun.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import site.code4fun.constant.AppConstants;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "stock_column")
public class FinancialColumnEntity {
    @Id
    private String id;
    private String type;
    private String name;
    private String unit;
    private boolean isDefault;
    private String fieldName;

    @JsonProperty("en_Type")
    private String enType;

    @JsonProperty("en_Name")
    private String enName;
    private String tagName;
    private String comTypeCode;

    @Column(name = "order_col")
    private int order;
}

