package ipleiria.risk_matrix.controller;
import ipleiria.risk_matrix.dto.SuggestionDTO;
import ipleiria.risk_matrix.models.sugestions.Suggestions;
import ipleiria.risk_matrix.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suggestions")
public class SuggestionController {

    @Autowired
    private SuggestionService suggestionService;

    // Criar uma nova sugestão associada a uma questão
    @PostMapping("/add/{questionId}")
    public SuggestionDTO addSuggestion(@PathVariable Long questionId, @RequestBody SuggestionDTO suggestionDTO) {
        return suggestionService.addSuggestionToQuestion(questionId, suggestionDTO);
    }
}
