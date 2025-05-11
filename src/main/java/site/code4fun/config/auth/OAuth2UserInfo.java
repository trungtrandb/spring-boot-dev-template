package site.code4fun.config.auth;

import lombok.Getter;

import java.util.Map;

@Getter
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    public abstract String getImageUrl();
    public String getId() {
        return (String) attributes.get("id");
    }
    public String getName() {
        return (String) attributes.get("name");
    }
    public String getEmail() {
        return (String) attributes.get("email");
    }
}