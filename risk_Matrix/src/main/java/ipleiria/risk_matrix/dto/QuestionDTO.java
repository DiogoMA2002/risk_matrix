package ipleiria.risk_matrix.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    // Remove the required categoryId and use categoryName instead.
    @NotBlank(message = "A categoria da pergunta é obrigatória.")
    @JsonProperty("category") // Map JSON field "category" to categoryName.
    private String categoryName;

    @NotNull(message = "A lista de opções é obrigatória.")
    @Size(min = 1, message = "Pelo menos uma opção deve ser fornecida.")
    private List<@Valid QuestionOptionDTO> options;

    public QuestionDTO() {}

    // When converting from the Question entity to DTO.
    public QuestionDTO(ipleiria.risk_matrix.models.questions.Question question) {
        this.id = question.getId();
        this.questionnaireId = question.getQuestionnaire().getId();
        this.questionText = question.getQuestionText();
        if (question.getCategory() != null) {
            // You can optionally set a categoryId here if needed.
            this.categoryName = question.getCategory().getName();
        }
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

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public List<QuestionOptionDTO> getOptions() { return options; }
    public void setOptions(List<QuestionOptionDTO> options) { this.options = options; }
}
