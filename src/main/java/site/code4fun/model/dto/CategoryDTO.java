package site.code4fun.model.dto;

import lombok.Data;
import site.code4fun.constant.Status;
import site.code4fun.model.CategoryEntity ;

import java.io.Serializable;
import java.util.Date;

@Data
public class CategoryDTO implements Serializable {

	private Long id;
	private String name;
	private String icon;
	private String slug;
	private Status status;
	private CategoryEntity.POSITION position;
	private Date created;
	private Date updated;
	private AttachmentDTO image;
}
