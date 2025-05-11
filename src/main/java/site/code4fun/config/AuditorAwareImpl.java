package site.code4fun.config;

import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import site.code4fun.model.User;
import site.code4fun.util.SecurityUtils;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<User> {

    @Override
    @NonNull
    public Optional<User> getCurrentAuditor() {
        return Optional.ofNullable(SecurityUtils.getUser());
    }
}