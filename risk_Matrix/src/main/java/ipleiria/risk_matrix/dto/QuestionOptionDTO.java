package ipleiria.risk_matrix.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ipleiria.risk_matrix.models.answers.Impact;
import ipleiria.risk_matrix.models.answers.Probability;
import ipleiria.risk_matrix.models.answers.Severity;
import ipleiria.risk_matrix.models.questions.QuestionOption;

import java.util.List;

public class QuestionOptionDTO {

    private String optionText;
    private Impact impact;
    private Probability probability;
    private Severity severity;
    private List<SuggestionDTO> suggestions;

    public QuestionOptionDTO() {}

    @JsonCreator
    public QuestionOptionDTO(
            @JsonProperty("optionText") String optionText,
            @JsonProperty("impact") Impact impact,
            @JsonProperty("probability") Probability probability,
            @JsonProperty("suggestions") List<SuggestionDTO> suggestions
    ) {
        this.optionText = optionText;
        this.impact = impact;
        this.probability = probability;
        this.suggestions = suggestions;
    }

    public QuestionOptionDTO(QuestionOption option) {
        this.optionText = option.getOptionText();
        this.impact = option.getImpact();
        this.probability = option.getProbability();
        this.severity = option.getSeverity();
        this.suggestions = option.getSuggestions().stream()
                .map(SuggestionDTO::new)
                .toList();
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Impact getImpact() {
        return impact;
    }

    public void setImpact(Impact impact) {
        this.impact = impact;
    }

    public Probability getProbability() {
        return probability;
    }

    public void setProbability(Probability probability) {
        this.probability = probability;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public List<SuggestionDTO> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<SuggestionDTO> suggestions) {
        this.suggestions = suggestions;
    }
}