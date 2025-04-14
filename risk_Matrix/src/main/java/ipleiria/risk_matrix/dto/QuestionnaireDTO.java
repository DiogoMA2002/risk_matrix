package ipleiria.risk_matrix.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class QuestionnaireDTO {

    // Getters and setters
    private Long id;

    @NotBlank(message = "Titulo não pode estar vázio")
    private String title;

    private List<@Valid QuestionDTO> questions;

    public QuestionnaireDTO() {
        // DO NOT DELETE - JACKSON
    }

    public QuestionnaireDTO(Questionnaire questionnaire) {
        this.id = questionnaire.getId();
        this.title = questionnaire.getTitle();
        this.questions = questionnaire.getQuestions() != null
                ? questionnaire.getQuestions().stream()
                .map(QuestionDTO::new)
                .collect(Collectors.toList())
                : List.of();
    }

}
