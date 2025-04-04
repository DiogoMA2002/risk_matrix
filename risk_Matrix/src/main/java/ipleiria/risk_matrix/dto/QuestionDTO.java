package ipleiria.risk_matrix.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ipleiria.risk_matrix.models.questions.QuestionCategory;
import ipleiria.risk_matrix.utils.QuestionCategoryDeserializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.stream.Collectors;

public class QuestionDTO {

    private Long id;

    private Long questionnaireId;

    @NotBlank(message = "O texto da pergunta não pode estar em branco.")
    private String questionText;

    @NotNull(message = "A categoria da pergunta é obrigatória.")
    @JsonDeserialize(using = QuestionCategoryDeserializer.class)
    private QuestionCategory category;

    @NotNull(message = "A lista de opções é obrigatória.")
    @Size(min = 1, message = "Pelo menos uma opção deve ser fornecida.")
    private List<@Valid QuestionOptionDTO> options;

    public QuestionDTO() {}

    public QuestionDTO(ipleiria.risk_matrix.models.questions.Question question) {
        this.id = question.getId();
        this.questionnaireId = question.getQuestionnaire().getId();
        this.questionText = question.getQuestionText();
        this.category = question.getCategory();
        this.options = question.getOptions().stream()
                .map(QuestionOptionDTO::new)
                .collect(Collectors.toList());
    }

    // Getters and setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuestionnaireId() { return questionnaireId; }
    public void setQuestionnaireId(Long questionnaireId) { this.questionnaireId = questionnaireId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public QuestionCategory getCategory() { return category; }
    public void setCategory(QuestionCategory category) { this.category = category; }

    public List<QuestionOptionDTO> getOptions() { return options; }
    public void setOptions(List<QuestionOptionDTO> options) { this.options = options; }
}
