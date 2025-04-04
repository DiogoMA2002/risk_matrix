package ipleiria.risk_matrix.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.models.questions.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class QuestionOptionDTO {

    @NotBlank(message = "O texto da opção não pode estar em branco.")
    private String optionText;

    @NotNull(message = "O tipo da opção é obrigatório.")
    private OptionLevelType optionType;

    @NotNull(message = "O nível da opção é obrigatório.")
    private OptionLevel optionLevel;

    // Optional – include only if you're capturing severity at the option level
    private Severity severity;

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

    public QuestionOptionDTO(QuestionOption option) {
        this.optionText = option.getOptionText();
        this.optionType = option.getOptionType();
        this.optionLevel = option.getOptionLevel();
        // Optional: severity if your model has it
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
