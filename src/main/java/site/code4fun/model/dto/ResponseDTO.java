package site.code4fun.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO implements Serializable {
	private Type type; // toast type at client
	private String title; // short, human-readable summary of the problem type
	private Integer code; // HTTP status code
	private String detail; // A human-readable explanation specific to this occurrence of the problem.
	private String instance; // URI reference that identifies the specific occurrence of the problem.  It may or may not yield further information if not referenced.

	public enum Type{
		basic, primary, success, warning, danger, info, control //NOSONAR
	}
}
