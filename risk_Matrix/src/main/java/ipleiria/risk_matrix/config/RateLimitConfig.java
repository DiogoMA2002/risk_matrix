package ipleiria.risk_matrix.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableScheduling
public class RateLimitConfig {

    // Rate limit configurations for different endpoint types
    public static final Map<String, RateLimitRule> RATE_LIMIT_RULES = createRateLimitRules();

    // Default rate limit for unmatched endpoints
    public static final RateLimitRule DEFAULT_RATE_LIMIT = new RateLimitRule(50, Duration.ofMinutes(1));

    // Bucket storage with TTL tracking
    private final Map<String, Map<String, BucketInfo>> bucketStorage = new ConcurrentHashMap<>();

    private static Map<String, RateLimitRule> createRateLimitRules() {
        Map<String, RateLimitRule> rules = new HashMap<>();
        
        // Authentication endpoints - strict limits
        rules.put("/api/auth/login", new RateLimitRule(5, Duration.ofMinutes(1)));
        rules.put("/api/auth/request-token", new RateLimitRule(3, Duration.ofMinutes(1)));
        rules.put("/api/auth/refresh", new RateLimitRule(10, Duration.ofMinutes(1)));
        rules.put("/api/auth/register", new RateLimitRule(2, Duration.ofMinutes(5)));
        rules.put("/api/auth/change-password", new RateLimitRule(5, Duration.ofMinutes(5)));
        
        // Data submission endpoints - moderate limits
        rules.put("/api/answers/submit-multiple", new RateLimitRule(10, Duration.ofMinutes(1)));
        rules.put("/api/feedback", new RateLimitRule(5, Duration.ofMinutes(1)));
        
        // Public read endpoints - generous limits
        rules.put("/api/questionnaires", new RateLimitRule(100, Duration.ofMinutes(1)));
        rules.put("/api/questions", new RateLimitRule(100, Duration.ofMinutes(1)));
        rules.put("/api/categories", new RateLimitRule(100, Duration.ofMinutes(1)));
        
        // Admin endpoints - moderate limits
        rules.put("/api/admin", new RateLimitRule(50, Duration.ofMinutes(1)));
        rules.put("/api/questionnaires/create", new RateLimitRule(20, Duration.ofMinutes(1)));
        rules.put("/api/questionnaires/import", new RateLimitRule(10, Duration.ofMinutes(1)));
        rules.put("/api/questionnaires/delete", new RateLimitRule(20, Duration.ofMinutes(1)));
        rules.put("/api/questions/create", new RateLimitRule(30, Duration.ofMinutes(1)));
        rules.put("/api/questions/delete", new RateLimitRule(30, Duration.ofMinutes(1)));
        rules.put("/api/categories/create", new RateLimitRule(20, Duration.ofMinutes(1)));
        rules.put("/api/categories/delete", new RateLimitRule(20, Duration.ofMinutes(1)));
        
        return rules;
    }

    @Bean
    public Map<String, Map<String, Bucket>> rateLimitBuckets() {
        return new ConcurrentHashMap<>();
    }

    // Cleanup expired buckets every 10 minutes
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void cleanupExpiredBuckets() {
        long now = System.currentTimeMillis();
        long cleanupThreshold = 30 * 60 * 1000; // 30 minutes of inactivity
        
        bucketStorage.entrySet().removeIf(endpointEntry -> {
            Map<String, BucketInfo> clientBuckets = endpointEntry.getValue();
            clientBuckets.entrySet().removeIf(clientEntry -> {
                BucketInfo bucketInfo = clientEntry.getValue();
                return (now - bucketInfo.getLastAccessTime()) > cleanupThreshold;
            });
            return clientBuckets.isEmpty();
        });
    }

    public BucketInfo getBucketInfo(String endpoint, String clientId) {
        Map<String, BucketInfo> clientBuckets = bucketStorage.computeIfAbsent(
            endpoint,
                _ -> new ConcurrentHashMap<>()
        );
        
        BucketInfo bucketInfo = clientBuckets.computeIfAbsent(
            clientId,
                _ -> {
                RateLimitRule rule = getRateLimitRule(endpoint);
                return new BucketInfo(rule.createBucket(), rule, System.currentTimeMillis());
            }
        );
        
        // Update last access time
        bucketInfo.setLastAccessTime(System.currentTimeMillis());
        return bucketInfo;
    }

    private RateLimitRule getRateLimitRule(String endpoint) {
        // Find specific rule for this endpoint using exact match first, then prefix match
        RateLimitRule rule = RATE_LIMIT_RULES.get(endpoint);
        if (rule != null) {
            return rule;
        }
        
        // Try prefix matching for dynamic endpoints
        for (Map.Entry<String, RateLimitRule> entry : RATE_LIMIT_RULES.entrySet()) {
            if (endpoint.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Return default rule if no specific rule found
        return DEFAULT_RATE_LIMIT;
    }

    @Getter
    public static class RateLimitRule {
        private final int requests;
        private final Duration duration;
        private final Bandwidth bandwidth;

        public RateLimitRule(int requests, Duration duration) {
            this.requests = requests;
            this.duration = duration;
            this.bandwidth = Bandwidth.classic(requests, Refill.greedy(requests, duration));
        }

        public Bucket createBucket() {
            return Bucket.builder().addLimit(bandwidth).build();
        }
    }

    @Getter
    public static class BucketInfo {
        private final Bucket bucket;
        private final RateLimitRule rateLimitRule;
        @Setter
        private long lastAccessTime;

        public BucketInfo(Bucket bucket, RateLimitRule rateLimitRule, long lastAccessTime) {
            this.bucket = bucket;
            this.rateLimitRule = rateLimitRule;
            this.lastAccessTime = lastAccessTime;
        }

    }
} 