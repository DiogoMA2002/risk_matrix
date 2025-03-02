package ipleiria.risk_matrix.models.answers;
import ipleiria.risk_matrix.models.questions.Question;
import jakarta.persistence.*;

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
}
