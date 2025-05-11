package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SupplierDTO implements Serializable {

	private Long id;
	private String name;
	private String phone;
	private String code;
	private String address;
	private String email;
	private AttachmentDTO image;
}
