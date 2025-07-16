package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.category.Category;
import ipleiria.risk_matrix.models.questions.Question;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class QuestionUserDTO {

    private Long id;
    private String questionText;
    private Category category;
    private String description;      // novo
    private String categoryLabel;    // novo
    private List<QuestionOptionUserDTO> options;

    public QuestionUserDTO() {
        // DO NOT DELETE - JACKSON
    }

    public QuestionUserDTO(Question question) {
        this.id = question.getId();
        this.questionText = question.getQuestionText();
        this.category = question.getCategory();
        this.description = question.getDescription();               // novo
        this.categoryLabel = question.getCategoryLabel();           // novo
        this.options = question.getOptions().stream()
                .map(QuestionOptionUserDTO::new)
                .collect(Collectors.toList());
    }
} 