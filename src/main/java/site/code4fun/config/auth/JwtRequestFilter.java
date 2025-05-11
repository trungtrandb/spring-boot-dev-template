package site.code4fun.config.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import site.code4fun.ApplicationProperties;
import site.code4fun.util.CookieUtils;
import site.code4fun.util.JwtTokenUtils;

import java.io.IOException;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtTokenUtils jwtTokenUtils;
    private final ApplicationProperties app;
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String jwtToken = getTokenFromRequest(request);
        try{
            if (isNotEmpty(jwtToken) && jwtTokenUtils.isValidToken(jwtToken)) {
                handleLoginWithJwt(jwtToken);
            }
        }catch (Exception e){
            log.warn(e.getMessage());
            CookieUtils.deleteCookie(request, response, "token",  app.isLocal() ? null : app.getAdminDomain());
            CookieUtils.deleteCookie(request, response, "isLogin", app.getCookieDomain());
        }

        chain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request){
        String requestTokenHeader = request.getHeader("Authorization");
        String jwtToken = null;
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer")) {
            jwtToken = requestTokenHeader.substring(7);
        }

        if (StringUtils.isBlank(jwtToken)){
            Optional<Cookie> tokenCookie = CookieUtils.getCookie(request, "token");
            if (tokenCookie.isPresent()){
                jwtToken = tokenCookie.get().getValue();
            }
        }
        return jwtToken;
    }

    private void handleLoginWithJwt(String token){
        UserDetails userDetails = jwtTokenUtils.getUserPrincipalFromToken(token);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}