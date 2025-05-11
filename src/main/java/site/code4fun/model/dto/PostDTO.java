package site.code4fun.model.dto;

import lombok.Data;
import site.code4fun.constant.PostType;
import site.code4fun.constant.Status;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Data
public class PostDTO implements Serializable {

	private Long id;
	private PostType type;
	private Status status;
	private CategoryDTO category;
	private Date created;
	private Date updated;
	private UserLite createdBy;
	private Map<String, PostContentDTO> postContents;
	private AttachmentDTO attachment;
	private AttachmentDTO banner;

}
