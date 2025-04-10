package ipleiria.risk_matrix.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionDTO {

    private Long id;

    // In a many-to-many relationship, a question can belong to multiple questionnaires.

    private List<Long> questionnaireIds = new ArrayList<>();

    @NotBlank(message = "O texto da pergunta não pode estar em branco.")
    private String questionText;

    // Use categoryName instead of a numeric categoryId.
    @NotBlank(message = "A categoria da pergunta é obrigatória.")
    @Pattern(regexp = "^[\\p{L}0-9 ]+$", message = "Nome inválido (sem caracteres especiais)")
    @JsonProperty("category") // Maps the JSON field "category" to categoryName.

    private String categoryName;

    @NotNull(message = "A lista de opções é obrigatória.")
    @Size(min = 1, message = "Pelo menos uma opção deve ser fornecida.")
    private List<@Valid QuestionOptionDTO> options;

    public QuestionDTO() {}

    // Constructor to convert from the Question entity to DTO.
    public QuestionDTO(ipleiria.risk_matrix.models.questions.Question question) {
        this.id = question.getId();
        if (question.getQuestionnaires() != null) {
            this.questionnaireIds = question.getQuestionnaires()
                    .stream()
                    .map(q -> q.getId())
                    .collect(Collectors.toList());
        }
        this.questionText = question.getQuestionText();
        if (question.getCategory() != null) {
            this.categoryName = question.getCategory().getName();
        }
        this.options = question.getOptions().stream()
                .map(QuestionOptionDTO::new)
                .collect(Collectors.toList());
    }

    // Getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public List<Long> getQuestionnaireIds() {
        return questionnaireIds;
    }
    public void setQuestionnaireIds(List<Long> questionnaireIds) {
        this.questionnaireIds = questionnaireIds;
    }

    public String getQuestionText() {
        return questionText;
    }
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getCategoryName() {
        return categoryName;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<QuestionOptionDTO> getOptions() {
        return options;
    }
    public void setOptions(List<QuestionOptionDTO> options) {
        this.options = options;
    }
}
