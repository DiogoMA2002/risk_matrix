package ipleiria.risk_matrix.config;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ipleiria.risk_matrix.exceptions.exception.RateLimitExceededException;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimitConfig rateLimitConfig;
    private final JwtUtil jwtUtil;
    private final Set<String> trustedProxies;

    private static final Pattern QUESTIONNAIRE_ID_PATTERN = Pattern.compile("/api/questionnaires/(\\d+)(/.*)?");
    private static final Pattern QUESTION_ID_PATTERN = Pattern.compile("/api/questions/(\\d+)(/.*)?");
    private static final Pattern CATEGORY_ID_PATTERN = Pattern.compile("/api/categories/(\\d+)(/.*)?");
    private static final Pattern ADMIN_PATTERN = Pattern.compile("/api/admin(/.*)?");

    public RateLimitInterceptor(RateLimitConfig rateLimitConfig, JwtUtil jwtUtil,
                                @Value("${app.trusted.proxies:127.0.0.1,::1}") String trustedProxiesConfig) {
        this.rateLimitConfig = rateLimitConfig;
        this.jwtUtil = jwtUtil;
        this.trustedProxies = Arrays.stream(trustedProxiesConfig.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String endpoint = getEndpointPath(request);
        String clientId = getClientId(request);

        if (shouldSkipRateLimit(endpoint)) {
            return true;
        }

        RateLimitConfig.BucketInfo bucketInfo = rateLimitConfig.getBucketInfo(endpoint, clientId);
        Bucket bucket = bucketInfo.getBucket();

        if (bucket.tryConsume(1)) {
            addRateLimitHeaders(response, bucket, bucketInfo.getRateLimitRule());
            return true;
        } else {
            logger.warn("Rate limit exceeded for endpoint: {} by client: {}", endpoint, clientId);
            throw new RateLimitExceededException("Too many requests. Please try again later.");
        }
    }

    private String getEndpointPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (QUESTIONNAIRE_ID_PATTERN.matcher(path).matches()) return "/api/questionnaires/{id}";
        if (QUESTION_ID_PATTERN.matcher(path).matches())     return "/api/questions/{id}";
        if (CATEGORY_ID_PATTERN.matcher(path).matches())     return "/api/categories/{id}";
        if (ADMIN_PATTERN.matcher(path).matches())           return "/api/admin";
        return path;
    }

    private String getClientId(HttpServletRequest request) {
        // Try to extract subject from JWT (cookie first, then Bearer header)
        String jwt = resolveJwt(request);
        if (jwt != null) {
            String subject = safeExtractSubject(jwt);
            if (subject != null) return subject;
        }

        // Fall back to IP address (only trust X-Forwarded-For from known proxies)
        return "ip:" + getClientIpAddress(request);
    }

    private String resolveJwt(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                String name = cookie.getName();
                if ("admin_access_token".equals(name) || "public_access_token".equals(name)) {
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

    private String safeExtractSubject(String jwt) {
        try {
            return jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            logger.debug("Could not extract subject from JWT for rate-limit key: {}", e.getMessage());
            return null;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        // Only trust forwarded headers when the direct connection comes from a trusted proxy
        if (trustedProxies.contains(remoteAddr)) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isBlank() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                // Take the rightmost (last) IP — added by the trusted proxy; leftmost can be spoofed by clients
                String[] ips = xForwardedFor.split(",");
                return ips[ips.length - 1].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isBlank() && !"unknown".equalsIgnoreCase(xRealIp)) {
                return xRealIp;
            }
        }

        return remoteAddr;
    }

    private boolean shouldSkipRateLimit(String endpoint) {
        return endpoint.startsWith("/actuator") ||
               endpoint.startsWith("/health") ||
               endpoint.startsWith("/error");
    }

    private void addRateLimitHeaders(HttpServletResponse response, Bucket bucket, RateLimitConfig.RateLimitRule rule) {
        try {
            long capacity = rule.getBandwidth().getCapacity();
            long availableTokens = bucket.getAvailableTokens();
            long resetTime = calculateResetTime(bucket, rule);

            response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(availableTokens));
            response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));
            response.setHeader("X-RateLimit-Reset-Seconds", String.valueOf((resetTime - System.currentTimeMillis()) / 1000));
        } catch (Exception e) {
            logger.warn("Failed to add rate limit headers: {}", e.getMessage());
        }
    }

    private long calculateResetTime(Bucket bucket, RateLimitConfig.RateLimitRule rule) {
        try {
            if (bucket.getAvailableTokens() == 0) {
                return System.currentTimeMillis() + rule.getDuration().toMillis();
            }
            return System.currentTimeMillis();
        } catch (Exception e) {
            logger.warn("Failed to calculate reset time: {}", e.getMessage());
            return System.currentTimeMillis() + 60_000;
        }
    }
}
