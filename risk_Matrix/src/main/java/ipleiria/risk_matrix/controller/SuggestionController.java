package ipleiria.risk_matrix.controller;
import ipleiria.risk_matrix.models.sugestions.Suggestion;
import ipleiria.risk_matrix.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suggestions")
public class SuggestionController {

    @Autowired
    private SuggestionService suggestionService;

    // Criar uma nova sugestão
    @PostMapping("/submit")
    public Suggestion submitSuggestion(@RequestBody Suggestion suggestion) {
        return suggestionService.submitSuggestion(suggestion);
    }

    // Obter todas as sugestões pendentes
    @GetMapping("/pending")
    public List<Suggestion> getPendingSuggestions() {
        return suggestionService.getPendingSuggestions();
    }

    // Obter todas as sugestões revisadas
    @GetMapping("/reviewed")
    public List<Suggestion> getReviewedSuggestions() {
        return suggestionService.getReviewedSuggestions();
    }

    // Marcar uma sugestão como revisada
    @PutMapping("/review/{id}")
    public void reviewSuggestion(@PathVariable Long id) {
        suggestionService.reviewSuggestion(id);
    }
}
