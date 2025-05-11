package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AttachmentDTO implements Serializable {
    private Long id;
    private String name;
    private String link;
    private String contentType;
    private Date created;
    private String status;
    private String thumbnail;
    private String author;
    private String content;
    private String storageProvider;
    private Long size;
    private Long view;
}
