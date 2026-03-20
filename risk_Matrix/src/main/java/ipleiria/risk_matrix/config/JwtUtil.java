package ipleiria.risk_matrix.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import ipleiria.risk_matrix.utils.RoleConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expirationMs}")
    private long jwtExpirationMs;

    @Value("${app.jwt.public.expirationMs:7200000}")
    private long publicTokenExpirationMs;

    @Value("${app.jwt.refresh.expirationMs:604800000}")
    private long refreshTokenExpirationMs;

    private SecretKey cachedSigningKey;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException(
                "JWT_SECRET environment variable is not set. The application cannot start without a signing key.");
        }
        this.cachedSigningKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private SecretKey signingKey() {
        return cachedSigningKey;
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", RoleConstants.ADMIN)
                .claim("type", RoleConstants.TOKEN_TYPE_ACCESS)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signingKey())
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", RoleConstants.ADMIN)
                .claim("type", RoleConstants.TOKEN_TYPE_REFRESH)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(signingKey())
                .compact();
    }

    public String generatePublicToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", RoleConstants.PUBLIC);
        claims.put("email", email);
        claims.put("type", RoleConstants.TOKEN_TYPE_ACCESS);

        return Jwts.builder()
                .subject(email)
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + publicTokenExpirationMs))
                .signWith(signingKey())
                .compact();
    }

    public String generatePublicRefreshToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", RoleConstants.PUBLIC);
        claims.put("email", email);
        claims.put("type", RoleConstants.TOKEN_TYPE_REFRESH);

        return Jwts.builder()
                .subject(email)
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(signingKey())
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public String extractTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    public String extractJti(String token) {
        return parseClaims(token).getId();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return RoleConstants.TOKEN_TYPE_REFRESH.equals(extractTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAdminTokenExpirationMs() {
        return jwtExpirationMs;
    }

    public long getPublicTokenExpirationMs() {
        return publicTokenExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
}
