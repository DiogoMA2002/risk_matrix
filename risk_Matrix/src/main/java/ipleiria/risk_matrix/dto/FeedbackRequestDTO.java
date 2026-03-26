package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.feedback.FeedbackType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Feedback cannot be blank")
    @Size(max = 1000, message = "Feedback must not exceed 1000 characters")
    private String userFeedback;

    @NotNull(message = "Feedback type is required")
    private FeedbackType feedbackType;
}
