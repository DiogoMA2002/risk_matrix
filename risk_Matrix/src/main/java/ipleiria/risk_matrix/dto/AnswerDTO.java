package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.Impact;
import ipleiria.risk_matrix.models.questions.Probability;
import ipleiria.risk_matrix.models.questions.Severity;
import ipleiria.risk_matrix.models.sugestions.Suggestions;

import java.util.List;
import java.util.stream.Collectors;

public class AnswerDTO {

    private Long id;
    private Long questionId;
    private String questionText;
    private String userResponse;
    private Impact impact;
    private Probability probability;
    private Severity severity;
    private String email;
    private List<SuggestionDTO> suggestions;

    public AnswerDTO() {}

    public AnswerDTO(Answer answer, List<Suggestions> optionSuggestions) {
        this.id = answer.getId();
        this.questionId = answer.getQuestionId();
        this.questionText = answer.getQuestionText();
        this.userResponse = answer.getUserResponse();
        this.impact = answer.getImpact();
        this.probability = answer.getProbability();
        this.severity = answer.getSeverity();
        this.email = answer.getEmail();

        // ✅ Map suggestions from the selected option
        this.suggestions = optionSuggestions.stream()
                .map(SuggestionDTO::new)
                .collect(Collectors.toList());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getUserResponse() { return userResponse; }
    public void setUserResponse(String userResponse) { this.userResponse = userResponse; }

    public Impact getImpact() { return impact; }
    public void setImpact(Impact impact) { this.impact = impact; }

    public Probability getProbability() { return probability; }
    public void setProbability(Probability probability) { this.probability = probability; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<SuggestionDTO> getSuggestions() { return suggestions; }
    public void setSuggestions(List<SuggestionDTO> suggestions) { this.suggestions = suggestions; }
}
