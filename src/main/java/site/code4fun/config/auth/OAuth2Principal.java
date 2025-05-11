package site.code4fun.config.auth;

import jakarta.persistence.Transient;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;
import site.code4fun.model.User;

import java.util.Map;

@Data
public class OAuth2Principal extends User implements OAuth2User {
    @Transient
    private Map<String, Object> attributes;

    @Override
    public String getName() {
        return getLastName() + " " + getFirstName();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public static OAuth2Principal fromUser(User user, Map<String, Object> attributes) {
        OAuth2Principal userPrincipal = new OAuth2Principal();
        BeanUtils.copyProperties(user, userPrincipal);
        userPrincipal.setAttributes(attributes);
        userPrincipal.setAuthorities(user.getAuthorities());
        return userPrincipal;
    }
}