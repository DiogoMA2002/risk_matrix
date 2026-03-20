package ipleiria.risk_matrix.repository;

import ipleiria.risk_matrix.models.users.BlockedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface BlockedTokenRepository extends JpaRepository<BlockedToken, Long> {

    Optional<BlockedToken> findByJti(String jti);

    @Modifying
    @Transactional
    @Query("DELETE FROM BlockedToken bt WHERE bt.expiresAt < :now")
    void deleteExpiredBefore(Instant now);
}
