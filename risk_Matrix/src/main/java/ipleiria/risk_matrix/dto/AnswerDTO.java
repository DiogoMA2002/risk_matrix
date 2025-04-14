package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class AnswerDTO {

    // Getters and Setters
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

}
