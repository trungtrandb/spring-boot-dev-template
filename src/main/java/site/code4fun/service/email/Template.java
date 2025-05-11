package site.code4fun.service.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Template(Long id,
                       String type,
                       String name,
                       @JsonProperty("drag_and_drop") boolean dragAndDrop,
                       boolean responsive,
                       String category,
                       boolean active,
                       String thumbnail,
                       @JsonProperty("content_type")String contentType,
                       String html
) {}
