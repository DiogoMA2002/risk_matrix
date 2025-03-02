package ipleiria.risk_matrix.repository;
import ipleiria.risk_matrix.models.sugestions.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

    // Busca todas as sugestões que ainda não foram revisadas
    List<Suggestion> findByReviewedFalse();

    // Busca todas as sugestões que já foram revisadas
    List<Suggestion> findByReviewedTrue();
}
