package ipleiria.risk_matrix.repository;
import ipleiria.risk_matrix.models.answers.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    // Busca respostas por pergunta
    List<Answer> findByQuestionId(Long questionId);

    List<Answer> findByEmail(String email);

    List<Answer> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);


}
