package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AnswerDTO {

    private Long id;

    @NotNull(message = "Question ID é Obrigatória")
    private Long questionId;

    private String questionText;

    @NotBlank(message = "User response cannot be blank")
    private String userResponse;

    private OptionLevelType questionType;

    private OptionLevel chosenLevel;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Formato de Email é inválido")
    private String email;

    private LocalDateTime createdAt;

    // New field: submissionId
    @NotBlank(message = "Submission ID é obrigatório")
    private String submissionId;

    public AnswerDTO(Answer answer) {
        this.id = answer.getId();
        this.questionId = answer.getQuestionId();
        this.questionText = answer.getQuestionText();
        this.userResponse = answer.getUserResponse();
        this.questionType = answer.getQuestionType();
        this.chosenLevel = answer.getChosenLevel();
        this.email = answer.getEmail();
        this.createdAt = answer.getCreatedAt();
        this.submissionId = answer.getSubmissionId();
    }

    public AnswerDTO() {
        // Required by Jackson
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getSubmissionId() {
        return submissionId;
    }
    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }
}
