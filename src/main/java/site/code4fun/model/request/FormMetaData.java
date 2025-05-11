package site.code4fun.model.request;

import lombok.Data;

@Data
public class FormMetaData {
    private Long id;
    private String providerId;
    private String name;
    private String provider;
    private String content;
    private String fbId;
    private String srcUrl;
}
