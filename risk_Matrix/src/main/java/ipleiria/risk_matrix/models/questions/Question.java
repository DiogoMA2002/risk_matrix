package ipleiria.risk_matrix.models.questions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ipleiria.risk_matrix.models.category.Category;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String questionText;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // Remove the old ManyToOne relationship to Questionnaire
    // and replace it with a ManyToMany mapping.
    @ManyToMany(mappedBy = "questions")
    @JsonIgnore
    private List<Questionnaire> questionnaires = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionOption> options = new ArrayList<>();

    public Question() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public List<Questionnaire> getQuestionnaires() { return questionnaires; }
    public void setQuestionnaires(List<Questionnaire> questionnaires) { this.questionnaires = questionnaires; }

    public List<QuestionOption> getOptions() { return options; }
    public void setOptions(List<QuestionOption> options) { this.options = options; }
}
