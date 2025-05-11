package site.code4fun.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import site.code4fun.model.User;

/**
 * Utility class for security-related operations.
 * This class provides methods to access current user information and security context.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityUtils {
    
    /**
     * Gets the current authenticated user.
     * @return The current user if authenticated, null otherwise
     */
    public static synchronized User getUser() {
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal instanceof User userPrincipal) {
                    return userPrincipal;
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return null;
        }
    }

    /**
     * Gets the current language from locale context.
     * @return The current language code
     */
    public static String getCurrentLang() {
        return LocaleContextHolder.getLocale().getLanguage();
    }

    /**
     * Gets the ID of the current user.
     * @return The user ID if authenticated, null otherwise
     */
    public static Long getUserId() {
        User currentUser = getUser();
        return currentUser != null ? currentUser.getId() : null;
    }

    /**
     * Checks if the current user is an admin.
     * @return true if the current user is an admin, false otherwise
     */
    public static boolean isAdmin() {
        User currentUser = getUser();
        return currentUser != null && currentUser.isSystemAdmin();
    }
}
