package ipleiria.risk_matrix.models.questions;
import com.fasterxml.jackson.annotation.JsonBackReference;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String questionText;

    @Enumerated(EnumType.STRING)
    private QuestionCategory category;

    @Column(nullable = false)
    private String userEmail;

    @ManyToOne
    @JoinColumn(name = "questionnaire_id") // Cria uma chave estrangeira na tabela Question
    @JsonBackReference
    private Questionnaire questionnaire;

    // Constructors
    public Question() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public QuestionCategory getCategory() { return category; }
    public void setCategory(QuestionCategory category) { this.category = category; }


    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }

    public void setQuestionnaire(Questionnaire questionnaire) {
        this.questionnaire = questionnaire;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}

