package ipleiria.risk_matrix.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailTokenRequestDTO {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    public EmailTokenRequestDTO() {}
    
    public EmailTokenRequestDTO(String email) {
        this.email = email;
    }

}