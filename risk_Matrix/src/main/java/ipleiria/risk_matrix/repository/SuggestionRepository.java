package ipleiria.risk_matrix.repository;
import ipleiria.risk_matrix.models.sugestions.Suggestions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestions, Long> {


}
