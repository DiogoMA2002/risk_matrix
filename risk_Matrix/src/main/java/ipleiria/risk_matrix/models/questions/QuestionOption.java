package ipleiria.risk_matrix.models.questions;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import ipleiria.risk_matrix.models.answers.Impact;
import ipleiria.risk_matrix.models.answers.Probability;
import ipleiria.risk_matrix.models.answers.Severity;
import ipleiria.risk_matrix.models.sugestions.Suggestions;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question_options")
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @JsonBackReference
    private Question question;

    @Column(nullable = false)
    private String optionText;

    @Enumerated(EnumType.STRING)
    private Impact impact;

    @Enumerated(EnumType.STRING)
    private Probability probability;

    @Transient
    private Severity severity;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Suggestions> suggestions = new ArrayList<>();

    public QuestionOption() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
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

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    // ✅ Calculate severity based on matrix
    public Severity getSeverity() {
        return switch (impact) {
            case HIGH -> switch (probability) {
                case HIGH -> Severity.CRITICAL; // Special case for high-high = Critical
                case MEDIUM -> Severity.HIGH;
                case LOW -> Severity.MEDIUM;
            };
            case MEDIUM -> switch (probability) {
                case HIGH -> Severity.HIGH;
                case MEDIUM -> Severity.MEDIUM;
                case LOW -> Severity.LOW;
            };
            case LOW -> switch (probability) {
                case HIGH -> Severity.MEDIUM;
                case MEDIUM, LOW -> Severity.LOW;
            };
        };
    }

    public List<Suggestions> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<Suggestions> suggestions) {
        this.suggestions = suggestions;
    }
}