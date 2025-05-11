package site.code4fun.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO extends UserLite {
	
	private Date birthday;
	private boolean enabled;
	private Date created;
	private Set<RoleDTO> roles;
	private ShiftDTO shift;
	private String status;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;
	private String confirmPassword;
}
