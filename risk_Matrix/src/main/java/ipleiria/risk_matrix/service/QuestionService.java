package ipleiria.risk_matrix.service;
import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionCategory;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.models.sugestions.Suggestions;
import ipleiria.risk_matrix.repository.QuestionRepository;
import ipleiria.risk_matrix.repository.QuestionnaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    // Criar uma nova pergunta
    public QuestionDTO createQuestion(QuestionDTO questionDTO) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionDTO.getQuestionnaireId())
                .orElseThrow(() -> new RuntimeException("Questionnaire not found"));
        Question question = new Question();
        question.setQuestionText(questionDTO.getQuestionText());
        question.setQuestionnaire(questionnaire); // ✅ Associate with Questionnaire
        try {
            question.setCategory(QuestionCategory.valueOf(questionDTO.getCategory()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid category: " + questionDTO.getCategory());
        }

        List<QuestionOption> options = questionDTO.getOptions().stream().map(optionDTO -> {
            QuestionOption option = new QuestionOption();
            option.setQuestion(question);
            option.setOptionText(optionDTO.getOptionText());
            option.setImpact(optionDTO.getImpact());
            option.setProbability(optionDTO.getProbability());

            // ✅ Add suggestions
            List<Suggestions> suggestions = optionDTO.getSuggestions().stream().map(suggestionDTO -> {
                Suggestions suggestion = new Suggestions();
                suggestion.setOption(option);
                suggestion.setSuggestionText(suggestionDTO.getSuggestionText());
                return suggestion;
            }).toList();

            option.setSuggestions(suggestions);

            return option;
        }).toList();

        question.setOptions(options);

        questionRepository.save(question);
        return new QuestionDTO(question);
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
