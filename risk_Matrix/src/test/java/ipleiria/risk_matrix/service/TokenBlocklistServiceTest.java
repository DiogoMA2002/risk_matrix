package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.models.users.BlockedToken;
import ipleiria.risk_matrix.repository.BlockedTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlocklistServiceTest {

    @Mock
    private BlockedTokenRepository repository;

    @InjectMocks
    private TokenBlocklistService tokenBlocklistService;

    @Test
    void block_nullJti_doesNothing() {
        tokenBlocklistService.block(null, System.currentTimeMillis() + 10000);
        verify(repository, never()).findByJti(anyString());
        verify(repository, never()).save(any(BlockedToken.class));
    }

    @Test
    void block_newJti_persistsAndCaches() {
        long expiry = System.currentTimeMillis() + 60_000;
        when(repository.findByJti("jti-1")).thenReturn(Optional.empty());

        tokenBlocklistService.block("jti-1", expiry);

        verify(repository).save(any(BlockedToken.class));
        assertThat(tokenBlocklistService.isBlocked("jti-1")).isTrue();
    }

    @Test
    void block_duplicateInsertRace_isSwallowed() {
        long expiry = System.currentTimeMillis() + 60_000;
        when(repository.findByJti("jti-race")).thenReturn(Optional.empty());
        when(repository.save(any(BlockedToken.class))).thenThrow(new DataIntegrityViolationException("dup"));

        assertThatCode(() -> tokenBlocklistService.block("jti-race", expiry))
                .doesNotThrowAnyException();
        assertThat(tokenBlocklistService.isBlocked("jti-race")).isTrue(); // still in cache
    }

    @Test
    void isBlocked_cacheHitExpired_returnsFalseWithoutRepoLookup() {
        long expired = System.currentTimeMillis() - 1000;
        when(repository.findByJti("jti-expired")).thenReturn(Optional.empty());

        tokenBlocklistService.block("jti-expired", expired);

        assertThat(tokenBlocklistService.isBlocked("jti-expired")).isFalse();
    }

    @Test
    void isBlocked_cacheMissDbFutureHit_returnsTrueAndCachesResult() {
        BlockedToken token = new BlockedToken();
        token.setJti("db-jti");
        token.setExpiresAt(Instant.ofEpochMilli(System.currentTimeMillis() + 30_000));

        when(repository.findByJti("db-jti")).thenReturn(Optional.of(token));

        assertThat(tokenBlocklistService.isBlocked("db-jti")).isTrue();
        assertThat(tokenBlocklistService.isBlocked("db-jti")).isTrue(); // cached path
        verify(repository, times(1)).findByJti("db-jti");
    }

    @Test
    void isBlocked_cacheMissDbExpiredHit_returnsFalse() {
        BlockedToken token = new BlockedToken();
        token.setJti("db-old");
        token.setExpiresAt(Instant.ofEpochMilli(System.currentTimeMillis() - 30_000));

        when(repository.findByJti("db-old")).thenReturn(Optional.of(token));

        assertThat(tokenBlocklistService.isBlocked("db-old")).isFalse();
    }

    @Test
    void cleanupExpiredTokens_callsRepositoryAndEvictsExpiredCache() {
        long past = System.currentTimeMillis() - 5000;
        long future = System.currentTimeMillis() + 5000;

        when(repository.findByJti("old")).thenReturn(Optional.empty());
        when(repository.findByJti("new")).thenReturn(Optional.empty());

        tokenBlocklistService.block("old", past);
        tokenBlocklistService.block("new", future);

        tokenBlocklistService.cleanupExpiredTokens();

        verify(repository).deleteExpiredBefore(any(Instant.class));
        assertThat(tokenBlocklistService.isBlocked("old")).isFalse();
        assertThat(tokenBlocklistService.isBlocked("new")).isTrue();
    }
}
