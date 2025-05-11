package site.code4fun.model;

import jakarta.persistence.*;
import lombok.Data;
import site.code4fun.constant.AppConstants;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "stock_symbol")
public class StockSymbolEntity extends Auditable {
    @Id
    private Integer id;
    private String organName;
    private String symbol;
    private String type;
    private String board;
    private String enOrganName;
    private String enOrganShortName;
    private String organShortName;
}
