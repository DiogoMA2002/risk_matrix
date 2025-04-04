package ipleiria.risk_matrix.models.answers;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.Severity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private String questionText;

    @Column(nullable = false)
    private String userResponse;

    // Indicate which type of question (Impact or Probability)
    @Enumerated(EnumType.STRING)
    private OptionLevelType questionType;

    // The user’s chosen level (LOW, MEDIUM, HIGH)
    @Enumerated(EnumType.STRING)
    private OptionLevel chosenLevel;

    @Column(nullable = false)
    private String email;

    // New field to uniquely identify a submission session.
    @Column(nullable = false)
    private String submissionId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    // Constructors
    public Answer() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getUserResponse() { return userResponse; }
    public void setUserResponse(String userResponse) { this.userResponse = userResponse; }


    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public OptionLevelType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(OptionLevelType questionType) {
        this.questionType = questionType;
    }

    public OptionLevel getChosenLevel() {
        return chosenLevel;
    }

    public void setChosenLevel(OptionLevel chosenLevel) {
        this.chosenLevel = chosenLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
