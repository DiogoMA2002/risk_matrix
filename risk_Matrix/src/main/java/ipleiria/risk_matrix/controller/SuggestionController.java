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

    @PostMapping("/add/{answerId}")
    public SuggestionDTO addSuggestion(@PathVariable Long answerId, @RequestBody SuggestionDTO suggestionDTO) {
        return suggestionService.addSuggestionToAnswer(answerId, suggestionDTO);
    }

    @GetMapping("/by-answer/{answerId}")
    public List<SuggestionDTO> getSuggestionsByAnswer(@PathVariable Long answerId) {
        return suggestionService.getSuggestionsByAnswer(answerId);
    }
}
