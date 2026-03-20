package ipleiria.risk_matrix.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import ipleiria.risk_matrix.service.TokenBlocklistService;
import ipleiria.risk_matrix.utils.RoleConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class PublicJwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(PublicJwtFilter.class);

    private final JwtUtil jwtUtil;
    private final TokenBlocklistService tokenBlocklistService;

    public PublicJwtFilter(JwtUtil jwtUtil, TokenBlocklistService tokenBlocklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlocklistService = tokenBlocklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = resolveToken(request);

        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtUtil.validateToken(jwt)) {
                    String role = jwtUtil.extractRole(jwt);

                    if (RoleConstants.PUBLIC.equals(role)) {
                        // Reject revoked access tokens
                        String jti = safeExtractJti(jwt);
                        if (jti != null && tokenBlocklistService.isBlocked(jti)) {
                            filterChain.doFilter(request, response);
                            return;
                        }

                        String email = jwtUtil.extractEmail(jwt);

                        UserDetails userDetails = User.builder()
                                .username(email)
                                .password("")
                                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PUBLIC")))
                                .build();

                        UsernamePasswordAuthenticationToken token =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(token);
                    }
                }
            } catch (Exception e) {
                logger.warn("Public JWT validation failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String safeExtractJti(String jwt) {
        try {
            return jwtUtil.extractJti(jwt);
        } catch (Exception e) {
            return null;
        }
    }

    /** Prefer HttpOnly cookie; fall back to Authorization header. */
    private String resolveToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("public_access_token".equals(cookie.getName())) {
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
}
