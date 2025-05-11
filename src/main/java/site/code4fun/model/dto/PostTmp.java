package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PostTmp implements Serializable { // Just use as adapter for shop UI, don't use to do anything
    private String faq_description;
    private String faq_title;
    private Object id;
    private String faq_type;
    private String language;
    private String slug;
    private List<String> translated_languages;
}
