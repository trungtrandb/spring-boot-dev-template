package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class InventoryDTO implements Serializable {

	private String id;
	private String content;
	private int quantity;
	private BigDecimal cost;
	private ProductDTO product;
	private Date created;
	private SupplierDTO supplier;
}
