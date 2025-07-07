package ipleiria.risk_matrix.models.questionnaire;

import ipleiria.risk_matrix.models.questions.Question;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "questionnaire")
public class Questionnaire {

    // Getters and setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @ManyToMany
    @JoinTable(
            name = "questionnaire_questions",
            joinColumns = @JoinColumn(name = "questionnaire_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions;

    public Questionnaire() {
        this.questions = new ArrayList<>();
    }

}
