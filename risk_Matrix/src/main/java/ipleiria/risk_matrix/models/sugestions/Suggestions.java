package ipleiria.risk_matrix.models.sugestions;
import com.fasterxml.jackson.annotation.JsonBackReference;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.Question;
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
    @JoinColumn(name="answer_id", nullable = false)
    @JsonBackReference
    private Answer answer;

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

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }
}
