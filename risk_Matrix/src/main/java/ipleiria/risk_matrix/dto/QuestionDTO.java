package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.questions.Question;

import java.util.List;
import java.util.stream.Collectors;

public class QuestionDTO {

    private Long id;
    private String questionText;
    private String category;
    private List<AnswerDTO> answers;



    // 🔥 Construtor padrão vazio (para Jackson)
    public QuestionDTO() {}

    public QuestionDTO(Question question) {
        this.id = question.getId();
        this.questionText = question.getQuestionText();
        this.category = question.getCategory().name();
        this.answers = question.getAnswers().stream()
                .map(AnswerDTO::new)
                .collect(Collectors.toList());
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

    public void setAnswers(List<AnswerDTO> answers) {
        this.answers = answers;
    }

    public List<AnswerDTO> getAnswers() {
        return answers;
    }
}
