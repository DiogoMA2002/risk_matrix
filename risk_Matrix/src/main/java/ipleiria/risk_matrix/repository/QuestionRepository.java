package ipleiria.risk_matrix.repository;
import ipleiria.risk_matrix.models.questions.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {


    Optional<Question> findByQuestionText(String questionText);

    boolean existsByCategoryId(Long id);

    /**
     * Find a question uniquely by its text and category id.
     * Used during questionnaire import to reuse existing questions and avoid duplicates.
     */
    Optional<Question> findByQuestionTextAndCategory_Id(String questionText, Long categoryId);

    /**
     * Find questions that are not associated to any questionnaire.
     * Useful to cleanup orphan questions after deleting a questionnaire.
     */
    List<Question> findAllByQuestionnairesIsEmpty();
}
