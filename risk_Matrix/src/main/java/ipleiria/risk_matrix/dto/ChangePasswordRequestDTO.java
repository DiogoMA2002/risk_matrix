package ipleiria.risk_matrix.dto;
import ipleiria.risk_matrix.annotations.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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


}
