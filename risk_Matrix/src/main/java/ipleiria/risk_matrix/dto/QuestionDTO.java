package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.Question;

import java.util.List;
import java.util.stream.Collectors;

public class QuestionDTO {

    private Long id;
    private Long questionnaireId;
    private String questionText;
    private String category;
    private List<QuestionOptionDTO> options;

    public QuestionDTO() {}

    public QuestionDTO(Question question) {
        this.id = question.getId();
        this.questionnaireId = question.getQuestionnaire().getId();
        this.questionText = question.getQuestionText();
        this.category = question.getCategory().name();
        this.options = question.getOptions().stream()
                .map(QuestionOptionDTO::new)
                .collect(Collectors.toList());
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuestionnaireId() { return questionnaireId; }
    public void setQuestionnaireId(Long questionnaireId) { this.questionnaireId = questionnaireId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<QuestionOptionDTO> getOptions() { return options; }
    public void setOptions(List<QuestionOptionDTO> options) { this.options = options; }
}
