package ipleiria.risk_matrix.repository;

import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {

    List<Questionnaire> findByTitleContainingIgnoreCase(String title);

    /**
     * Load questionnaire with its questions and their categories in one query.
     * Question options are batch-loaded via @BatchSize on the Question entity.
     */
    @EntityGraph(attributePaths = {"questions", "questions.category"})
    @Query("SELECT DISTINCT q FROM Questionnaire q WHERE q.id = :id")
    Optional<Questionnaire> findByIdWithDetails(@Param("id") Long id);

    /**
     * Load all questionnaires with their questions and categories.
     */
    @EntityGraph(attributePaths = {"questions", "questions.category"})
    @Query("SELECT DISTINCT q FROM Questionnaire q")
    List<Questionnaire> findAllWithDetails();
}
