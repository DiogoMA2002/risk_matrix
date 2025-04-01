package ipleiria.risk_matrix.models.questionnaire;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import ipleiria.risk_matrix.models.questions.Question;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questionnaire")
public class Questionnaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @OneToMany(mappedBy = "questionnaire", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JsonManagedReference
    private List<Question> questions = new ArrayList<>();

    public Questionnaire() {
        this.questions = new ArrayList<>();
    }

    public Questionnaire(List<Question> questions) {
        this.questions = questions != null ? questions : new ArrayList<>();
    }


    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
