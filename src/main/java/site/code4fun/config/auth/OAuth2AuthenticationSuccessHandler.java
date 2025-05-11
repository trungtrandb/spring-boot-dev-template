package site.code4fun.config.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import site.code4fun.ApplicationProperties;
import site.code4fun.model.User;
import site.code4fun.util.CookieUtils;
import site.code4fun.util.JwtTokenUtils;

import java.io.IOException;

import static site.code4fun.constant.AppConstants.REDIRECT_URI_PARAM_COOKIE_NAME;


@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenUtils jwtTokenUtils;
    private final ApplicationProperties app;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String targetUrl = CookieUtils
                .getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME).map(Cookie::getValue) // TODO check trusted URL before redirect
                .orElse(getDefaultTargetUrl());

        User userPrincipal = (User) authentication.getPrincipal();

        var tokenDTO = jwtTokenUtils.generateToken(userPrincipal);

        CookieUtils.addLoginCookie(response, tokenDTO.getAccessToken(), app.getAdminDomain());
        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        CookieUtils.removeAuthorizationRequestCookies(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}