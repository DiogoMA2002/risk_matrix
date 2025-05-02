package ipleiria.risk_matrix.models.answers;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "answers")
public class Answer {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long questionId;

    @Column
    private Long questionOptionId;

    @Column(nullable = false)
    private String questionText;

    @Column(nullable = false)
    private String userResponse;

    // Indicate which type of question (Impact or Probability)
    @Enumerated(EnumType.STRING)
    private OptionLevelType questionType;

    // The user’s chosen level (LOW, MEDIUM, HIGH)
    @Enumerated(EnumType.STRING)
    private OptionLevel chosenLevel;

    @Column(nullable = false)
    private String email;

    // New field to uniquely identify a submission session.
    @Column(nullable = false)
    private String submissionId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Answer() {}


}
