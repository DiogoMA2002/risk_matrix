package ipleiria.risk_matrix.models.feedback;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // No setter — DB-managed

    @NotBlank(message = "Feedback cannot be blank")
    @Size(max = 1000, message = "Feedback must not exceed 1000 characters")
    @Column(nullable = false)
    @Setter
    private String userFeedback;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(nullable = false)
    @Setter
    private String email;

    @NotNull(message = "Feedback type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private FeedbackType feedbackType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // No setter — auto-generated
}
