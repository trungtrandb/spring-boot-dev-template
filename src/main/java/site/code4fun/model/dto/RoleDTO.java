package site.code4fun.model.dto;

import lombok.Data;
import site.code4fun.constant.Status;

import java.io.Serializable;
import java.util.List;

@Data
public class RoleDTO implements Serializable {
    private Long id;
    private String name;
    private String title;
    private Status status;
    private List<String> privileges;
}
