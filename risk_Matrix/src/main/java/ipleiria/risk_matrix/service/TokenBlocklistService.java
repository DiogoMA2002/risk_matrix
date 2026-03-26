package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.models.users.BlockedToken;
import ipleiria.risk_matrix.repository.BlockedTokenRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JPA-backed blocklist for revoked token JTIs with an in-memory first-pass cache.
 * Cache avoids a DB round-trip on every authenticated request.
 * Survives application restarts via the DB layer; expired rows are purged on a schedule.
 */
@Service
public class TokenBlocklistService {

    private final BlockedTokenRepository repository;

    /** jti → expiry epoch-ms.  Entries evicted by the cleanup scheduler. */
    private final ConcurrentHashMap<String, Long> cache = new ConcurrentHashMap<>();

    public TokenBlocklistService(BlockedTokenRepository repository) {
        this.repository = repository;
    }

    public void block(String jti, long expiresAtMs) {
        if (jti == null) return;

        cache.put(jti, expiresAtMs);

        try {
            if (repository.findByJti(jti).isEmpty()) {
                BlockedToken entry = new BlockedToken();
                entry.setJti(jti);
                entry.setExpiresAt(Instant.ofEpochMilli(expiresAtMs));
                repository.save(entry);
            }
        } catch (DataIntegrityViolationException ignored) {
            // Concurrent insert — already persisted by another thread; cache entry is sufficient.
        }
    }

    public boolean isBlocked(String jti) {
        if (jti == null) return false;

        Long cachedExpiry = cache.get(jti);
        if (cachedExpiry != null) {
            return cachedExpiry > System.currentTimeMillis();
        }

        // Cache miss — check DB (happens after restart before cache is warm)
        return repository.findByJti(jti)
                .map(bt -> {
                    if (bt.getExpiresAt().isAfter(Instant.now())) {
                        cache.put(jti, bt.getExpiresAt().toEpochMilli());
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    /** Purge expired entries from both DB and cache — runs every 10 minutes. */
    @Scheduled(fixedDelay = 600_000)
    public void cleanupExpiredTokens() {
        repository.deleteExpiredBefore(Instant.now());

        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue() <= now) {
                it.remove();
            }
        }
    }
}
