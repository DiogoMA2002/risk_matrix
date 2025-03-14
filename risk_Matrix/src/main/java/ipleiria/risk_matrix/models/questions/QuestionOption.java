package ipleiria.risk_matrix.models.questions;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

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
    @Column(nullable = false)
    private OptionLevelType optionType;  // IMPACT or PROBABILITY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OptionLevel optionLevel; // LOW, MEDIUM, HIGH


    public QuestionOption() {
    }

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

    public OptionLevelType getOptionType() {
        return optionType;
    }

    public void setOptionType(OptionLevelType optionType) {
        this.optionType = optionType;
    }

    public OptionLevel getOptionLevel() {
        return optionLevel;
    }

    public void setOptionLevel(OptionLevel optionLevel) {
        this.optionLevel = optionLevel;
    }
}