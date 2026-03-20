package ipleiria.risk_matrix.config;

import ipleiria.risk_matrix.service.AdminUserDetailsService;
import ipleiria.risk_matrix.service.TokenBlocklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import ipleiria.risk_matrix.utils.RoleConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final AdminUserDetailsService userDetailsService;
    private final TokenBlocklistService tokenBlocklistService;

    public JwtFilter(JwtUtil jwtUtil, AdminUserDetailsService userDetailsService, TokenBlocklistService tokenBlocklistService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tokenBlocklistService = tokenBlocklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = resolveToken(request);

        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String role = jwtUtil.extractRole(jwt);
                if (!isAdminRoleClaim(role)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // Reject revoked tokens
                String jti = safeExtractJti(jwt);
                if (jti != null && tokenBlocklistService.isBlocked(jti)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String username = jwtUtil.extractUsername(jwt);
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken token =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(token);
                    }
                }
            } catch (Exception e) {
                logger.warn("Admin JWT validation failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /** Prefer HttpOnly cookie; fall back to Authorization header. */
    private String resolveToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("admin_access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String safeExtractJti(String jwt) {
        try {
            return jwtUtil.extractJti(jwt);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Accept legacy and current role claim formats:
     * - admin / ADMIN
     * - ROLE_ADMIN
     */
    private boolean isAdminRoleClaim(String roleClaim) {
        if (roleClaim == null || roleClaim.isBlank()) {
            return false;
        }
        String normalized = roleClaim.toUpperCase(Locale.ROOT);
        return "ADMIN".equals(normalized) || "ROLE_ADMIN".equals(normalized);
    }
}
