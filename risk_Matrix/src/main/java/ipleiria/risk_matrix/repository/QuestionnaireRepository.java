package ipleiria.risk_matrix.repository;

import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
    List<Questionnaire> findByTitleContainingIgnoreCase(String title);
}
