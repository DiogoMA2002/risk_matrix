package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.questionnaire.Questionnaire;

import java.util.List;
import java.util.stream.Collectors;

public class QuestionnaireDTO {

    private Long id;
    private String title;
    private List<QuestionDTO> questions;

    // 🔥 Construtor padrão vazio (para Jackson)
    public QuestionnaireDTO() {}

    public QuestionnaireDTO(Questionnaire questionnaire) {
        this.id = questionnaire.getId();
        this.title = questionnaire.getTitle();
        this.questions = questionnaire.getQuestions() != null
                ? questionnaire.getQuestions().stream()
                .map(QuestionDTO::new)
                .collect(Collectors.toList())
                : List.of(); // Retorna lista vazia se for nulo
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<QuestionDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDTO> questions) {
        this.questions = questions;
    }
}
