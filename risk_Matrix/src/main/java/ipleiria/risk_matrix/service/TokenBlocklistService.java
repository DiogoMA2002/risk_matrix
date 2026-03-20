package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.models.users.BlockedToken;
import ipleiria.risk_matrix.repository.BlockedTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * JPA-backed blocklist for revoked refresh token JTIs.
 * Survives application restarts; expired rows are purged on a schedule.
 */
@Service
public class TokenBlocklistService {

    private final BlockedTokenRepository repository;

    public TokenBlocklistService(BlockedTokenRepository repository) {
        this.repository = repository;
    }

    public void block(String jti, long expiresAtMs) {
        if (jti == null) return;
        if (repository.findByJti(jti).isPresent()) return; // already blocked

        BlockedToken entry = new BlockedToken();
        entry.setJti(jti);
        entry.setExpiresAt(Instant.ofEpochMilli(expiresAtMs));
        repository.save(entry);
    }

    public boolean isBlocked(String jti) {
        if (jti == null) return false;
        return repository.findByJti(jti)
                .map(bt -> bt.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }

    /** Purge rows whose tokens have already expired naturally — runs every 10 minutes. */
    @Scheduled(fixedDelay = 600_000)
    public void cleanupExpiredTokens() {
        repository.deleteExpiredBefore(Instant.now());
    }
}
