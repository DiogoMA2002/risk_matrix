package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.questions.Severity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public class UserAnswersDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotNull(message = "Answers list is required")
    @Size(min = 1, message = "At least one answer is required")
    private List<@Valid AnswerDTO> answers;

    private Map<String, Severity> severitiesByCategory;

    public UserAnswersDTO() {
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public List<AnswerDTO> getAnswers() {
        return answers;
    }
    public void setAnswers(List<AnswerDTO> answers) {
        this.answers = answers;
    }

    public Map<String, Severity> getSeveritiesByCategory() {
        return severitiesByCategory;
    }
    public void setSeveritiesByCategory(Map<String, Severity> severitiesByCategory) {
        this.severitiesByCategory = severitiesByCategory;
    }
}
