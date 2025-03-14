package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.Impact;
import ipleiria.risk_matrix.models.questions.Probability;
import ipleiria.risk_matrix.models.questions.Severity;

public class AnswerDTO {

    private Long id;
    private Long questionId;
    private String questionText;
    private String userResponse;
    private Impact impact;
    private Probability probability;
    private Severity severity;
    private String email;

    public AnswerDTO() {}

    public AnswerDTO(Answer answer) {
        this.id = answer.getId();
        this.questionId = answer.getQuestionId();
        this.questionText = answer.getQuestionText();
        this.userResponse = answer.getUserResponse();
        this.impact = answer.getImpact();
        this.probability = answer.getProbability();
        this.severity = answer.getSeverity();
        this.email = answer.getEmail();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getUserResponse() { return userResponse; }
    public void setUserResponse(String userResponse) { this.userResponse = userResponse; }

    public Impact getImpact() { return impact; }
    public void setImpact(Impact impact) { this.impact = impact; }

    public Probability getProbability() { return probability; }
    public void setProbability(Probability probability) { this.probability = probability; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
   }
