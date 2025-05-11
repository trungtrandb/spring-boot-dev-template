package site.code4fun.model.dto;

import lombok.Data;

@Data
public class IntegrationDTO {

    private String name;
    private String description;
    private String imageUrl;
    private boolean active;
    private boolean installed;
    private String type;
}
