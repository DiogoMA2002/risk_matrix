package ipleiria.risk_matrix.models.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "blocked_token", indexes = @Index(columnList = "jti", unique = true))
public class BlockedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String jti;

    /** Millisecond epoch when the original token would have expired naturally. */
    @Column(nullable = false)
    private Instant expiresAt;
}
