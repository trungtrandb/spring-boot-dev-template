package site.code4fun.model.dto;

import lombok.Data;
import site.code4fun.constant.ProcessingStatus;

import java.util.Date;

@Data
public class FaceDataDTO{
    private Long id;
    private String name;
    private String content;
    private String srcUrl;
    private String thumbnail;
    private Date created;
    private ProcessingStatus status;
}