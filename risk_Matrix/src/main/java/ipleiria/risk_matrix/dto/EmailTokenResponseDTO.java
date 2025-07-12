package ipleiria.risk_matrix.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailTokenResponseDTO {
    private String token;
    private String refreshToken;
    private String email;
    private String role;
    private long expiresAt;
    private long refreshExpiresAt;

    public EmailTokenResponseDTO(String token, String refreshToken, String email, String role, long expiresAt, long refreshExpiresAt) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.email = email;
        this.role = role;
        this.expiresAt = expiresAt;
        this.refreshExpiresAt = refreshExpiresAt;
    }
}