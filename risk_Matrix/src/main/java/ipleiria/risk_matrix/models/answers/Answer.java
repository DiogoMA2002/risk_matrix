package ipleiria.risk_matrix.models.answers;
import ipleiria.risk_matrix.models.questions.Impact;
import ipleiria.risk_matrix.models.questions.Probability;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.models.questions.Severity;
import ipleiria.risk_matrix.models.sugestions.Suggestions;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    private Impact impact;

    @Enumerated(EnumType.STRING)
    private Probability probability;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(nullable = false)
    private String email;

    @Transient
    private List<Suggestions> suggestions = new ArrayList<>();

    // Constructors
    public Answer() {}

    public void setSuggestionsFromOption(QuestionOption option) {
        this.suggestions = option != null ? option.getSuggestions() : new ArrayList<>();
    }

    public List<Suggestions> getSuggestions() {
        return suggestions;
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

    public Impact getImpact() { return impact; }
    public void setImpact(Impact impact) { this.impact = impact; }

    public Probability getProbability() { return probability; }
    public void setProbability(Probability probability) { this.probability = probability; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }



}
