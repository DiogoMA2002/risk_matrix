package ipleiria.risk_matrix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateUsernameRequestDTO {

    @NotBlank(message = "O novo nome de utilizador não pode estar em branco.")
    @Size(min = 3, max = 50, message = "O nome de utilizador deve ter entre 3 e 50 caracteres.")
    private String newUsername;

    @NotBlank(message = "A senha atual é obrigatória.")
    private String currentPassword;

    // Getters and Setters
    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
} 