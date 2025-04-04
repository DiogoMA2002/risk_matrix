package ipleiria.risk_matrix.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;

import java.util.List;
import java.util.stream.Collectors;

public class QuestionnaireDTO {

    private Long id;

    @NotBlank(message = "Titulo não pode estar vázio")
    private String title;

    private List<@Valid QuestionDTO> questions;

    public QuestionnaireDTO() {}

    public QuestionnaireDTO(Questionnaire questionnaire) {
        this.id = questionnaire.getId();
        this.title = questionnaire.getTitle();
        this.questions = questionnaire.getQuestions() != null
                ? questionnaire.getQuestions().stream()
                .map(QuestionDTO::new)
                .collect(Collectors.toList())
                : List.of();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<QuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }
}
