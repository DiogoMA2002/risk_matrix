package ipleiria.risk_matrix.repository;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Busca todas as perguntas por categoria
    List<Question> findByCategory(QuestionCategory category);

    // Verifica se uma pergunta já existe (para evitar duplicatas)
    boolean existsByQuestionText(String questionText);
}
