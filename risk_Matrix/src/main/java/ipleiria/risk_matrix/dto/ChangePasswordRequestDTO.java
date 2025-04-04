package ipleiria.risk_matrix.dto;
import ipleiria.risk_matrix.annotations.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@PasswordMatches  // Custom annotation (defined below)
public class ChangePasswordRequestDTO {

    @NotBlank(message = "A senha atual é obrigatória.")
    private String oldPassword;

    @NotBlank(message = "A nova senha é obrigatória.")
    @Size(min = 8, message = "A nova senha deve ter pelo menos 8 caracteres.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$",
            message = "A nova senha deve conter letras maiúsculas, minúsculas, um número e um caractere especial."
    )
    private String newPassword;

    @NotBlank(message = "A confirmação da nova senha é obrigatória.")
    private String confirmNewPassword;

    // Getters and setters

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }
}
