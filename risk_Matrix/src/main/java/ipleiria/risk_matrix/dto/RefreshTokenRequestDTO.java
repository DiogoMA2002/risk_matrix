package ipleiria.risk_matrix.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RefreshTokenRequestDTO {
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
    
    private String email; // For public users
    
    public RefreshTokenRequestDTO() {}
    
    public RefreshTokenRequestDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public RefreshTokenRequestDTO(String refreshToken, String email) {
        this.refreshToken = refreshToken;
        this.email = email;
    }
} 