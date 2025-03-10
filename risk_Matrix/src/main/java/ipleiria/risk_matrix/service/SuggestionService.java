package ipleiria.risk_matrix.service;
import ipleiria.risk_matrix.dto.SuggestionDTO;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.sugestions.Suggestions;
import ipleiria.risk_matrix.repository.QuestionRepository;
import ipleiria.risk_matrix.repository.SuggestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SuggestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SuggestionRepository suggestionRepository;

    // Criar uma nova sugestão
    public SuggestionDTO addSuggestionToQuestion(Long questionId, SuggestionDTO suggestionDTO) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Pergunta não encontrada"));

        Suggestions suggestion = new Suggestions();
        suggestion.setSuggestionText(suggestionDTO.getSuggestionText());
        suggestion.setQuestion(question);

        suggestionRepository.save(suggestion);
        return new SuggestionDTO(suggestion);
    }
}
