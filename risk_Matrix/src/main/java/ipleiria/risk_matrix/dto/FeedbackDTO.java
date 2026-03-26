package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.feedback.Feedback;
import ipleiria.risk_matrix.models.feedback.FeedbackType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FeedbackDTO {
    private Long id;
    private String userFeedback;
    private String email;
    private FeedbackType feedbackType;
    private LocalDateTime createdAt;

    public FeedbackDTO() {
    }

    public FeedbackDTO(Feedback feedback) {
        this.id = feedback.getId();
        this.userFeedback = feedback.getUserFeedback();
        this.email = feedback.getEmail();
        this.feedbackType = feedback.getFeedbackType();
        this.createdAt = feedback.getCreatedAt();
    }
}
