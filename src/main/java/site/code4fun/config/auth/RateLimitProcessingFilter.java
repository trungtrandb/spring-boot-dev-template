package site.code4fun.config.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import site.code4fun.ApplicationProperties;

import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitProcessingFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Long> redisTemplate;
    private static final int MAX_REQUESTS = 1000;
    private final ApplicationProperties properties;


    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain chain) throws IOException, ServletException {
        if (isRateLimited(request) && !properties.isLocal()){
            response.setStatus(TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded. Try again later.");
            return;
        }
        chain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilterAsyncDispatch() { // Apply filter to async request
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {// Apply filter to error request
        return false;
    }

    private boolean isRateLimited(HttpServletRequest request) { // Fixed window
        String key = String.format("rl_%s_%s", request.getRemoteHost(), request.getRequestURI());

        String script = """
                local current = redis.call('INCR', KEYS[1])
                if current == '1' then
                    redis.call('EXPIRE', KEYS[1], ARGV[1])
                end
                return tonumber(current) > tonumber(ARGV[2])
                """;

        RedisScript<Boolean> redisScript = new DefaultRedisScript<>(script, Boolean.class);

        return redisTemplate.execute(
                redisScript,
                Collections.singletonList(key),
                Duration.ofMinutes(1).toSeconds(),
                MAX_REQUESTS
        );
    }



}
