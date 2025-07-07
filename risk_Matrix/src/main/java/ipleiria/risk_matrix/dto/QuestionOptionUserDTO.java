package ipleiria.risk_matrix.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionOptionUserDTO {

    // Getters and Setters
    @NotBlank(message = "O texto da opção não pode estar em branco.")
    private String optionText;

    @NotNull(message = "O tipo da opção é obrigatório.")
    private OptionLevelType optionType;

    @NotNull(message = "O nível da opção é obrigatório.")
    private OptionLevel optionLevel;

    // Note: recommendation is intentionally excluded for user-facing endpoints

    public QuestionOptionUserDTO() {
        // DO NOT DELETE - JACKSON
    }

    @JsonCreator
    public QuestionOptionUserDTO(
            @JsonProperty("optionText") String optionText,
            @JsonProperty("optionType") OptionLevelType optionType,
            @JsonProperty("optionLevel") OptionLevel optionLevel
    ) {
        this.optionText = optionText;
        this.optionType = optionType;
        this.optionLevel = optionLevel;
    }

    public QuestionOptionUserDTO(QuestionOption option) {
        this.optionText = option.getOptionText();
        this.optionType = option.getOptionType();
        this.optionLevel = option.getOptionLevel();
        // recommendation is intentionally excluded
    }
} 