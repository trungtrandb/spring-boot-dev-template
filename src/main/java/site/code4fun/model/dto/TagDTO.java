package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TagDTO implements Serializable {

	private Long id;
	private String name;
	private String icon;
	private String slug;
}
