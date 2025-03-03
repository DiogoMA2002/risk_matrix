package ipleiria.risk_matrix.service;
import ipleiria.risk_matrix.models.sugestions.Suggestion;
import ipleiria.risk_matrix.repository.SuggestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SuggestionService {

    @Autowired
    private SuggestionRepository suggestionRepository;

    // Criar uma nova sugestão
    public Suggestion submitSuggestion(Suggestion suggestion) {
        suggestion.setSubmittedAt(LocalDateTime.now());
        return suggestionRepository.save(suggestion);
    }

    // Buscar todas as sugestões não revisadas
    public List<Suggestion> getPendingSuggestions() {
        return suggestionRepository.findByReviewedFalse();
    }

    // Buscar todas as sugestões revisadas
    public List<Suggestion> getReviewedSuggestions() {
        return suggestionRepository.findByReviewedTrue();
    }

    // Marcar sugestão como revisada
    public void reviewSuggestion(Long id) {
        Optional<Suggestion> suggestion = suggestionRepository.findById(id);
        if (suggestion.isPresent()) {
            suggestion.get().setReviewed(true);
            suggestionRepository.save(suggestion.get());
        } else {
            throw new RuntimeException("Sugestão não encontrada!");
        }
    }
}
