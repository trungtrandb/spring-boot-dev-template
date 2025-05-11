package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class BannerDTO implements Serializable {

    private Long id;
    private String title;
    private int typeId;
    private String description;
    private AttachmentDTO image;
}
