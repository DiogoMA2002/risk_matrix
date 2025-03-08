package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.questions.Question;

public class QuestionDTO {

    private Long id;
    private String questionText;
    private String category;

    // 🔥 Construtor padrão vazio (para Jackson)
    public QuestionDTO() {}

    public QuestionDTO(Question question) {
        this.id = question.getId();
        this.questionText = question.getQuestionText();
        this.category = question.getCategory().name();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
