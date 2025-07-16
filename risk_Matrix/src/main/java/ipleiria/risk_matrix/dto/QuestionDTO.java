package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class QuestionDTO {

    // Getters and setters
    private Long id;

    // In a many-to-many relationship, a question can belong to multiple questionnaires.

    private List<Long> questionnaireIds = new ArrayList<>();

    @NotBlank(message = "O texto da pergunta não pode estar em branco.")
    private String questionText;

    // Use categoryName instead of a numeric categoryId.
    @NotBlank(message = "A categoria da pergunta é obrigatória.")
    @Pattern(regexp = "^[\\p{L}0-9 ]+$", message = "Nome inválido (sem caracteres especiais)")
    private String categoryName;

    @NotNull(message = "A lista de opções é obrigatória.")
    @Size(min = 1, message = "Pelo menos uma opção deve ser fornecida.")
    private List<@Valid QuestionOptionDTO> options;

    @NotBlank(message = "O rótulo da categoria (label) é obrigatório.")
    private String categoryLabel;

    private String description;

    public QuestionDTO() {
        // DO NOT DELETE - Jackson
    }

    // Constructor to convert from the Question entity to DTO.
    public QuestionDTO(ipleiria.risk_matrix.models.questions.Question question) {
        this.id = question.getId();
        if (question.getQuestionnaires() != null) {
            this.questionnaireIds = question.getQuestionnaires()
                    .stream()
                    .map(Questionnaire::getId)
                    .collect(Collectors.toList());
        }
        this.questionText = question.getQuestionText();
        if (question.getCategory() != null) {
            this.categoryName = question.getCategory().getName();
        }

        this.categoryLabel = question.getCategoryLabel();
        this.description = question.getDescription();

        this.options = question.getOptions().stream()
                .map(QuestionOptionDTO::new)
                .collect(Collectors.toList());
    }

}
