package ipleiria.risk_matrix.models.answers;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @JsonBackReference
    private Question question;

    @Column(nullable = false)
    private String userResponse; // Ex: Sim, Não, Parcialmente

    @Enumerated(EnumType.STRING)
    private Impact impact; // Impacto

    @Enumerated(EnumType.STRING)
    private Probability probability;

    @Enumerated(EnumType.STRING)
    private Serverity serverity;

    @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Suggestions> suggestions = new ArrayList<>();

    @Column(nullable = false)
    private String email;

    // Constructors
    public Answer() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public String getUserResponse() { return userResponse; }
    public void setUserResponse(String userResponse) { this.userResponse = userResponse; }

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

    public List<Suggestions> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<Suggestions> suggestions) {
        this.suggestions = suggestions;
    }

    public Serverity getServerity() {
        return serverity;
    }

    public void setServerity(Serverity serverity) {
        this.serverity = serverity;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
