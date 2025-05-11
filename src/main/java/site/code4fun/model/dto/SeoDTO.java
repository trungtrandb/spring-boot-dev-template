package site.code4fun.model.dto;

import lombok.Data;

@Data
public class SeoDTO{
    public String ogImage;
    public String ogTitle;
    public String metaTags;
    public String metaTitle;
    public String canonicalUrl;
    public String ogDescription;
    public String twitterHandle;
    public String metaDescription;
    public String twitterCardType;
}
