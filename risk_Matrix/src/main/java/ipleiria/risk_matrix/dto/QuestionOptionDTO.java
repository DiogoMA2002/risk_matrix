package ipleiria.risk_matrix.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ipleiria.risk_matrix.models.questions.Impact;
import ipleiria.risk_matrix.models.questions.Probability;
import ipleiria.risk_matrix.models.questions.Severity;
import ipleiria.risk_matrix.models.questions.QuestionOption;

import java.util.List;

public class QuestionOptionDTO {

    private String optionText;
    private Impact impact;
    private Probability probability;
    private Severity severity;

    public QuestionOptionDTO() {}

    @JsonCreator
    public QuestionOptionDTO(
            @JsonProperty("optionText") String optionText,
            @JsonProperty("impact") Impact impact,
            @JsonProperty("probability") Probability probability
    ) {
        this.optionText = optionText;
        this.impact = impact;
        this.probability = probability;
    }

    public QuestionOptionDTO(QuestionOption option) {
        this.optionText = option.getOptionText();
        this.impact = option.getImpact();
        this.probability = option.getProbability();
        this.severity = option.getSeverity();
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

}