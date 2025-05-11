package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class StockSymbolDTO implements Serializable {
    private Integer id;
    private String organName;
    private String symbol;
    private String type;
    private String board;
    private String enOrganName;
    private String enOrganShortName;
    private String organShortName;
    private Date updated;
}
