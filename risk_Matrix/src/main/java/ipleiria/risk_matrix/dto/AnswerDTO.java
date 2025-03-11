package ipleiria.risk_matrix.dto;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.answers.Impact;
import ipleiria.risk_matrix.models.answers.Probability;
import ipleiria.risk_matrix.models.answers.Severity;

import java.util.List;

@JsonPropertyOrder({
        "id",
        "userResponse",
        "impact",
        "probability",
        "serverity",
})
public class AnswerDTO {

    private Long id;
    private Long questionId;
    private String questionText;
    private String userResponse;
    private Impact impact;
    private Probability probability;
    private Severity severity;
    private String email;

    // ✅ Default constructor (needed for Jackson)
    public AnswerDTO() {}

    // ✅ Constructor for output
    public AnswerDTO(Answer answer) {
        this.id = answer.getId();
        this.questionId = answer.getQuestionId();
        this.questionText = answer.getQuestionText();
        this.userResponse = answer.getUserResponse();
        this.impact = answer.getImpact();
        this.probability = answer.getProbability();
        this.severity = answer.getServerity();
        this.email = answer.getEmail();
    }

    // ✅ Constructor for input (Deserialization)
    @JsonCreator
    public AnswerDTO(
            @JsonProperty("userResponse") String userResponse,
            @JsonProperty("impact") Impact impact,
            @JsonProperty("probability") Probability probability,
            @JsonProperty("email") String email) {
        this.email = email;
        this.userResponse = userResponse;
        this.impact = impact;
        this.probability = probability;
    }

    // ✅ Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(String userResponse) {
        this.userResponse = userResponse;
    }

    public Impact getImpact() {
        return impact;
    }

    public void setImpact(Impact impact) {
        this.impact = impact;
    }

    public Probability getProbability() {
        return probability;
    }

    public void setProbability(Probability probability) {
        this.probability = probability;
    }

    public Severity getServerity() {
        return severity;
    }

    public void setServerity(Severity severity) {
        this.severity = severity;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
