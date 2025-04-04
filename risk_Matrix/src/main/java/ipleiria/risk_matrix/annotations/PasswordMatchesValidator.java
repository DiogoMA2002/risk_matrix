package ipleiria.risk_matrix.annotations;
import ipleiria.risk_matrix.dto.ChangePasswordRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, ChangePasswordRequestDTO> {

    @Override
    public boolean isValid(ChangePasswordRequestDTO request, ConstraintValidatorContext context) {
        if (request.getNewPassword() == null || request.getConfirmNewPassword() == null) {
            return false;
        }
        return request.getNewPassword().equals(request.getConfirmNewPassword());
    }
}
