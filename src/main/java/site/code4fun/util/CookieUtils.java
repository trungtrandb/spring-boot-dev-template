package site.code4fun.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.InternetDomainName;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.jackson2.SecurityJackson2Modules;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static site.code4fun.constant.AppConstants.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME;
import static site.code4fun.constant.AppConstants.REDIRECT_URI_PARAM_COOKIE_NAME;
import static site.code4fun.util.JwtTokenUtils.JWT_TOKEN_VALIDITY;

@Slf4j
public class CookieUtils {

    private static final ObjectMapper OBJECT_MAPPER;
    private static final String DEFAULT_PATH = "/";
    private static final boolean DEFAULT_SECURE = true;

    static {
        ClassLoader loader = CookieUtils.class.getClassLoader();
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModules(SecurityJackson2Modules.getModules(loader));
    }

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        return Optional.ofNullable(cookies)
            .flatMap(c -> Arrays.stream(c)
                .filter(cookie -> cookie.getName().equalsIgnoreCase(name))
                .findFirst());
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        addCookie(response, name, value, maxAge, true);
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge, boolean httpOnly) {
        addCookie(response, name, value, maxAge, httpOnly, null);
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge, boolean httpOnly, String domain) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(DEFAULT_PATH);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(DEFAULT_SECURE);
        cookie.setDomain(domain);
        cookie.setMaxAge(maxAge);
        
        response.addCookie(cookie);
        log.debug("Added cookie: name={}, domain={}, maxAge={}", name, domain, maxAge);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        deleteCookie(request, response, name, null);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name, String domain) {

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath(DEFAULT_PATH);
                    cookie.setMaxAge(0);
                    cookie.setSecure(DEFAULT_SECURE);

                    if (isNotBlank(domain)) {
                        cookie.setDomain(domain);
                    }

                    response.addCookie(cookie);
                    log.debug("Deleted cookie: name={}, domain={}", name, domain);
                }
            }
        }
    }

    public static String serialize(Object object) {
        try {
            return Base64.getUrlEncoder().encodeToString(OBJECT_MAPPER.writeValueAsBytes(object));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object: {}", object, e);
            throw new IllegalArgumentException("The given Json object value: "
                    + object + " cannot be transformed to a String", e);
        }
    }

    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(cookie.getValue());
            return OBJECT_MAPPER.readValue(decodedBytes, cls);
        } catch (IOException e) {
            log.error("Failed to deserialize cookie: {}", cookie.getName(), e);
            throw new IllegalArgumentException("The given string value: "
                    + cookie.getValue() + " cannot be transformed to Json object", e);
        }
    }

    public static void addLoginCookie(HttpServletResponse response, String accessToken, String subDomain) {
        String domain = RegexUtils.parseHost(subDomain);
        if (subDomain != null && subDomain.contains("https://")) {
            domain = InternetDomainName.from(domain).topDomainUnderRegistrySuffix().toString();
        }

        addCookie(response, "token", accessToken, JWT_TOKEN_VALIDITY, true, domain);
        addCookie(response, "isLogin", "true", JWT_TOKEN_VALIDITY, false, domain);
        log.info("Added login cookies for domain: {}", domain);
    }

    public static void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
        log.info("Removed authorization request cookies");
    }
}
