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

    @NotBlank(message = "O submissionId é obrigatório.")
    private String submissionId;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "O e-mail fornecido é inválido.")
    private String email;

    @NotNull(message = "A lista de respostas é obrigatória.")
    @Size(min = 1, message = "Pelo menos uma resposta deve ser fornecida.")
    private List<@Valid AnswerDTO> answers;

    private Map<String, Severity> severitiesByCategory;

    public UserAnswersDTO() {
    }

    public String getSubmissionId() {
        return submissionId;
    }
    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
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
