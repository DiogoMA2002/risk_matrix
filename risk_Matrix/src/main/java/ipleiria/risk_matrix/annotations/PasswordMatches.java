package ipleiria.risk_matrix.annotations;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatches {
    String message() default "New password and confirmation do not match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
