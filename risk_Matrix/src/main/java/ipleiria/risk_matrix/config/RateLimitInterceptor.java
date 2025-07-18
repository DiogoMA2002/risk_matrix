package ipleiria.risk_matrix.config;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


import java.util.regex.Pattern;
import ipleiria.risk_matrix.exceptions.exception.RateLimitExceededException;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimitConfig rateLimitConfig;

    // Pre-compiled patterns for better performance
    private static final Pattern QUESTIONNAIRE_ID_PATTERN = Pattern.compile("/api/questionnaires/(\\d+)(/.*)?");
    private static final Pattern QUESTION_ID_PATTERN = Pattern.compile("/api/questions/(\\d+)(/.*)?");
    private static final Pattern CATEGORY_ID_PATTERN = Pattern.compile("/api/categories/(\\d+)(/.*)?");
    private static final Pattern ADMIN_PATTERN = Pattern.compile("/api/admin(/.*)?");

    public RateLimitInterceptor(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String endpoint = getEndpointPath(request);
        String clientId = getClientId(request);

        // Skip rate limiting for certain endpoints if needed
        if (shouldSkipRateLimit(endpoint)) {
            return true;
        }

        RateLimitConfig.BucketInfo bucketInfo = rateLimitConfig.getBucketInfo(endpoint, clientId);
        Bucket bucket = bucketInfo.getBucket();
        
        if (bucket.tryConsume(1)) {
            // Add rate limit headers to response
            addRateLimitHeaders(response, bucket, bucketInfo.getRateLimitRule());
            return true;
        } else {
            // Rate limit exceeded
            logger.warn("Rate limit exceeded for endpoint: {} by client: {}", endpoint, clientId);
            throw new RateLimitExceededException("Too many requests. Please try again later.");
        }
    }

    private String getEndpointPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Use pre-compiled patterns for better performance and accuracy
        if (QUESTIONNAIRE_ID_PATTERN.matcher(path).matches()) {
            return "/api/questionnaires/{id}";
        }
        if (QUESTION_ID_PATTERN.matcher(path).matches()) {
            return "/api/questions/{id}";
        }
        if (CATEGORY_ID_PATTERN.matcher(path).matches()) {
            return "/api/categories/{id}";
        }
        if (ADMIN_PATTERN.matcher(path).matches()) {
            return "/api/admin";
        }
        
        return path;
    }

    private String getClientId(HttpServletRequest request) {
        // Try to get user ID from JWT token first
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            String userId = extractUserIdFromJwt(jwt);
            if (userId != null) {
                return userId; // Use user ID as primary identifier
            }
        }
        
        // Fallback to IP address
        String clientIp = getClientIpAddress(request);
        return "ip:" + clientIp; // Prefix to distinguish from user IDs
    }

    private String extractUserIdFromJwt(String jwt) {
        try {
            // Simple JWT parsing - you might want to use a proper JWT library
            String[] parts = jwt.split("\\.");
            if (parts.length == 3) {
                // Decode payload (base64url)
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                // Extract username from payload - adjust based on your JWT structure
                if (payload.contains("\"sub\":")) {
                    int start = payload.indexOf("\"sub\":\"") + 7;
                    int end = payload.indexOf("\"", start);
                    if (start > 6 && end > start) {
                        return payload.substring(start, end);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to extract user ID from JWT: {}", e.getMessage());
        }
        return null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private boolean shouldSkipRateLimit(String endpoint) {
        // Skip rate limiting for health checks or other internal endpoints
        return endpoint.startsWith("/actuator") || 
               endpoint.startsWith("/health") || 
               endpoint.startsWith("/error");
    }

    private void addRateLimitHeaders(HttpServletResponse response, Bucket bucket, RateLimitConfig.RateLimitRule rule) {
        try {
            // Get bandwidth information - use the stored rule information
            long capacity = rule.getBandwidth().getCapacity();
            long availableTokens = bucket.getAvailableTokens();
            long resetTime = calculateResetTime(bucket, rule);
            
            // Set correct rate limit headers
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
            // If bucket is empty, calculate when it will refill
            if (bucket.getAvailableTokens() == 0) {
                // Use the stored duration from our rate limit rule
                return System.currentTimeMillis() + rule.getDuration().toMillis();
            }
            
            // If bucket has tokens, reset time is now
            return System.currentTimeMillis();
        } catch (Exception e) {
            logger.warn("Failed to calculate reset time: {}", e.getMessage());
            return System.currentTimeMillis() + 60000; // Fallback to 1 minute
        }
    }
} 