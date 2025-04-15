package ipleiria.risk_matrix.repository;

import ipleiria.risk_matrix.models.feedback.Feedback;
import ipleiria.risk_matrix.models.feedback.FeedbackType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByEmail(String email);

    List<Feedback> findByFeedbackType(FeedbackType type);

    List<Feedback> findByEmailAndFeedbackType(String email, FeedbackType type);

    List<Feedback> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Feedback> findByEmailAndCreatedAtBetween(String email, LocalDateTime startDate, LocalDateTime endDate);

    List<Feedback> findByFeedbackTypeAndCreatedAtBetween(FeedbackType type, LocalDateTime startDate, LocalDateTime endDate);

    List<Feedback> findByEmailAndFeedbackTypeAndCreatedAtBetween(String email, FeedbackType type, LocalDateTime startDate, LocalDateTime endDate);
}
