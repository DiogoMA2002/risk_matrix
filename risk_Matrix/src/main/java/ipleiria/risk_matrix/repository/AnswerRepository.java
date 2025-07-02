package ipleiria.risk_matrix.repository;
import ipleiria.risk_matrix.models.answers.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {


    List<Answer> findByEmail(String email);

    List<Answer> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);


    List<Answer> findBySubmissionId(String submissionId);
}
