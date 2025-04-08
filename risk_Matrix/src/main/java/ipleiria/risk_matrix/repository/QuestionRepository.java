package ipleiria.risk_matrix.repository;
import ipleiria.risk_matrix.models.questions.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {


    Optional<Question> findByQuestionText(String questionText);

}
