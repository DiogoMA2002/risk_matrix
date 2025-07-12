package ipleiria.risk_matrix.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthResponseDTO {
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;

    public AuthResponseDTO(String token, String refreshToken, long expiresIn) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public AuthResponseDTO(String token) {
        this.token = token;
    }
}
