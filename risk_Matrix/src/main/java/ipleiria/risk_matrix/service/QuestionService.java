package ipleiria.risk_matrix.service;
import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionCategory;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    // Criar uma nova pergunta
    public Question createQuestion(Question question) {
        if (questionRepository.existsByQuestionText(question.getQuestionText())) {
            throw new RuntimeException("Pergunta já existe!");
        }
        return questionRepository.save(question);
    }

    // Buscar todas as perguntas
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    // Buscar todas as perguntas com sugestões
    public List<QuestionDTO> getAllQuestionsWithSuggestion() {
        return questionRepository.findAll()
                .stream()
                .map(QuestionDTO::new)
                .collect(Collectors.toList());

    }

    // Buscar perguntas por categoria
    public List<Question> getQuestionsByCategory(QuestionCategory category) {
        return questionRepository.findByCategory(category);
    }

    // Buscar pergunta por ID
    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    // Deletar uma pergunta
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }
}
