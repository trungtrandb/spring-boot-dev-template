package site.code4fun.model.dto;

import lombok.Data;
import site.code4fun.constant.Status;

import java.io.Serializable;

@Data
public class PostContentDTO implements Serializable {

    private String id;
    private String slug;
    private String lang;
    private String content;
    private String description;
    private String name;
    private Post post;
    private boolean autoTranslate;
    public record Post(String id, String name, Status status){}
}


