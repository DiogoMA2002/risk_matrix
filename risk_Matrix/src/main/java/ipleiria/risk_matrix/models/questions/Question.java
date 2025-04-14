package ipleiria.risk_matrix.models.questions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ipleiria.risk_matrix.models.category.Category;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "questions")
public class Question {

    // Getters and setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
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

}
