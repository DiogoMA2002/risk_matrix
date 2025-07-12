package ipleiria.risk_matrix.dto;

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
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
} 