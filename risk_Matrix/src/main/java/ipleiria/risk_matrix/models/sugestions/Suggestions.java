package ipleiria.risk_matrix.models.sugestions;
import com.fasterxml.jackson.annotation.JsonBackReference;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "suggestions")
public class Suggestions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A sugestão não pode ser nula")
    @Column(nullable = false)
    private String suggestionText;

    @ManyToOne
    @JoinColumn(name="question_id", nullable = false)
    @JsonBackReference
    private QuestionOption option;


    // Constructors
    public Suggestions() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getSuggestionText() {
        return suggestionText;
    }

    public void setSuggestionText(String suggestionText) {
        this.suggestionText = suggestionText;
    }

    public QuestionOption getOption() {
        return option;
    }

    public void setOption(QuestionOption option) {
        this.option = option;
    }


}
