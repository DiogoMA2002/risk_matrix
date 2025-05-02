package ipleiria.risk_matrix.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.models.questions.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionOptionDTO {

    // Getters and Setters
    @NotBlank(message = "O texto da opção não pode estar em branco.")
    private String optionText;

    @NotNull(message = "O tipo da opção é obrigatório.")
    private OptionLevelType optionType;

    @NotNull(message = "O nível da opção é obrigatório.")
    private OptionLevel optionLevel;

    // Optional – include only if you're capturing severity at the option level
    private Severity severity;

    private String recommendation; // ✅ novo campo


    public QuestionOptionDTO() {
        // DO NOT DELETE - JACKSON

    }

    @JsonCreator
    public QuestionOptionDTO(
            @JsonProperty("optionText") String optionText,
            @JsonProperty("optionType") OptionLevelType optionType,
            @JsonProperty("optionLevel") OptionLevel optionLevel,
            @JsonProperty("recommendation") String recommendation

    ) {
        this.optionText = optionText;
        this.optionType = optionType;
        this.optionLevel = optionLevel;
        this.recommendation = recommendation;
    }

    public QuestionOptionDTO(QuestionOption option) {
        this.optionText = option.getOptionText();
        this.optionType = option.getOptionType();
        this.optionLevel = option.getOptionLevel();
        this.recommendation = option.getRecommendation();
        // Optional: severity if your model has it
        // this.severity = option.getSeverity();
    }

}
