package site.code4fun.model.dto;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import site.code4fun.constant.PostType;

@Data
public class PostLiteDTO implements Serializable {

	private Long id;
	private PostType type;
	private CategoryDTO category;
	private Date created;
	private Date updated;
	private String slug;
	private String lang;
	private String content;
	private String name;
	private String description;
	private UserLite createdBy;
	private AttachmentDTO attachment;
	private AttachmentDTO banner;

//	public record CategoryDTO(Long id, String name, String slug) {}
}
