package ipleiria.risk_matrix.service;
import ipleiria.risk_matrix.dto.SuggestionDTO;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.sugestions.Suggestions;
import ipleiria.risk_matrix.repository.AnswerRepository;
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
    private AnswerRepository answerRepository;

    @Autowired
    private SuggestionRepository suggestionRepository;

    // Criar uma nova sugestão
    public SuggestionDTO addSuggestionToAnswer(Long answerId, SuggestionDTO suggestionDTO) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        Suggestions suggestion = new Suggestions();
        suggestion.setSuggestionText(suggestionDTO.getSuggestionText());
        suggestion.setAnswer(answer);

        suggestion = suggestionRepository.save(suggestion);
        return new SuggestionDTO(suggestion);
    }

    public List<SuggestionDTO> getSuggestionsByAnswer(Long answerId) {
        return suggestionRepository.findAll()
                .stream()
                .filter(suggestion -> suggestion.getAnswer().getId().equals(answerId))
                .map(SuggestionDTO::new)
                .toList();
    }
}
