package ipleiria.risk_matrix.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailTokenResponseDTO {
    
    private String token;
    private String email;
    private String role;
    private long expiresAt;
    
    public EmailTokenResponseDTO() {}
    
    public EmailTokenResponseDTO(String token, String email, String role, long expiresAt) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.expiresAt = expiresAt;
    }

}