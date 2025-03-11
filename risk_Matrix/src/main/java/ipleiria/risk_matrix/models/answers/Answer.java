package ipleiria.risk_matrix.models.answers;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.Question;
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

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private String userResponse; // Ex: Sim, Não, Parcialmente

    @Enumerated(EnumType.STRING)
    private RiskLevel calculatedRisk; // Resultado da avaliação

    @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Suggestions> suggestions = new ArrayList<>();


    // Constructors
    public Answer() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public String getUserResponse() { return userResponse; }
    public void setUserResponse(String userResponse) { this.userResponse = userResponse; }

    public RiskLevel getCalculatedRisk() { return calculatedRisk; }
    public void setCalculatedRisk(RiskLevel calculatedRisk) { this.calculatedRisk = calculatedRisk; }

    public List<Suggestions> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<Suggestions> suggestions) {
        this.suggestions = suggestions;
    }
}
