package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.questions.Severity;

import java.util.List;
import java.util.Map;

public class UserAnswersDTO {

    private String email;
    private List<AnswerDTO> answers;
    private Map<String, Severity> severitiesByCategory;

    public UserAnswersDTO() {
    }

    public UserAnswersDTO(String email, List<AnswerDTO> answers, Map<String, Severity> severitiesByCategory) {
        this.email = email;
        this.answers = answers;
        this.severitiesByCategory = severitiesByCategory;
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
