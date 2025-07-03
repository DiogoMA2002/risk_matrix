package ipleiria.risk_matrix.models.questions;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "question_options")
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @JsonBackReference
    private Question question;

    @Column(nullable = false)
    private String optionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OptionLevelType optionType;  // IMPACT or PROBABILITY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OptionLevel optionLevel; // LOW, MEDIUM, HIGH

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    public QuestionOption() {
    }

}