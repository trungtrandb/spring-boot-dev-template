package site.code4fun.model.dto;

import lombok.Data;
import site.code4fun.constant.Status;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ProductDTO implements Serializable {

	private Long id;
	private String name;
	private String slug;
	private Status status;
	private String content;
	private String unit;
	private String type;
	private String sku;
	private BigDecimal price;
	private BigDecimal cost;
	private BigDecimal discount;
	private Integer quantity;
	private Date created;
	private Date updated;
	private String thumbnail;
	private List<AttachmentDTO> files;
	private List<CategoryDTO> categories;
	private List<TagDTO> tags;
	private SupplierDTO supplier;
}
