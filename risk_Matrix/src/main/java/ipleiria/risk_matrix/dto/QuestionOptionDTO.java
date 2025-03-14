package ipleiria.risk_matrix.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.Severity;

public class QuestionOptionDTO {

    private String optionText;
    private OptionLevelType optionType;  // IMPACT or PROBABILITY
    private OptionLevel optionLevel;     // LOW, MEDIUM, HIGH
    private Severity severity;           // optional if you compute per-option

    public QuestionOptionDTO() {}

    @JsonCreator
    public QuestionOptionDTO(
            @JsonProperty("optionText") String optionText,
            @JsonProperty("optionType") OptionLevelType optionType,
            @JsonProperty("optionLevel") OptionLevel optionLevel
    ) {
        this.optionText = optionText;
        this.optionType = optionType;
        this.optionLevel = optionLevel;
    }

    // Create from an existing entity
    public QuestionOptionDTO(QuestionOption option) {
        this.optionText = option.getOptionText();
        this.optionType = option.getOptionType();
        this.optionLevel = option.getOptionLevel();
        // If you store severity in QuestionOption (less common if you do matrix calc later)
        // this.severity = option.getSeverity();
    }

    // Getters and Setters
    public String getOptionText() {
        return optionText;
    }
    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public OptionLevelType getOptionType() {
        return optionType;
    }
    public void setOptionType(OptionLevelType optionType) {
        this.optionType = optionType;
    }

    public OptionLevel getOptionLevel() {
        return optionLevel;
    }
    public void setOptionLevel(OptionLevel optionLevel) {
        this.optionLevel = optionLevel;
    }

    public Severity getSeverity() {
        return severity;
    }
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
}
