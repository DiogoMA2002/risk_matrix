package ipleiria.risk_matrix.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UpdateEmailRequestDTO {

    @NotBlank(message = "O novo email não pode estar em branco.")
    @Email(message = "Formato de email inválido.")
    private String newEmail;

    @NotBlank(message = "A senha atual é obrigatória.")
    private String currentPassword;

    // Getters and Setters
    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
} 