package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;

public class AnswerDTO {

    private Long id;
    private Long questionId;
    private String questionText;
    private String userResponse;

    private OptionLevelType questionType; // IMPACT or PROBABILITY
    private OptionLevel chosenLevel;      // LOW, MEDIUM, HIGH
    private String email;

    public AnswerDTO() {}

    public AnswerDTO(Answer answer) {
        this.id = answer.getId();
        this.questionId = answer.getQuestionId();
        this.questionText = answer.getQuestionText();
        this.userResponse = answer.getUserResponse();
        this.questionType = answer.getQuestionType();
        this.chosenLevel = answer.getChosenLevel();
        this.email = answer.getEmail();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getUserResponse() { return userResponse; }
    public void setUserResponse(String userResponse) { this.userResponse = userResponse; }

    public OptionLevelType getQuestionType() { return questionType; }
    public void setQuestionType(OptionLevelType questionType) { this.questionType = questionType; }

    public OptionLevel getChosenLevel() { return chosenLevel; }
    public void setChosenLevel(OptionLevel chosenLevel) { this.chosenLevel = chosenLevel; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
