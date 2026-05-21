package ipleiria.risk_matrix.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Locale;
import java.util.Optional;

public final class AuthUtils {

    private AuthUtils() {}

    public static Optional<String> getPublicUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        boolean isPublic = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(RoleConstants.ROLE_PUBLIC::equals);

        if (!isPublic) {
            return Optional.empty();
        }

        String email = auth.getName();
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(normalizeEmail(email));
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
