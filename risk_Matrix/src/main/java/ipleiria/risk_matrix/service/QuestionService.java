package ipleiria.risk_matrix.service;
import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionCategory;
import ipleiria.risk_matrix.models.questions.QuestionOption;
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
    public QuestionDTO createQuestion(Long questionnaireId, QuestionDTO questionDTO) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new RuntimeException("Questionnaire not found"));

        Question question = new Question();
        question.setQuestionText(questionDTO.getQuestionText());
        try {
            question.setCategory(QuestionCategory.valueOf(questionDTO.getCategory()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid category: " + questionDTO.getCategory());
        }
        question.setQuestionnaire(questionnaire);

        // ✅ Create options if available
        List<QuestionOption> options = questionDTO.getOptions().stream().map(optionDTO -> {
            QuestionOption option = new QuestionOption();
            option.setQuestion(question);
            option.setOptionText(optionDTO.getOptionText());
            option.setImpact(optionDTO.getImpact());
            option.setProbability(optionDTO.getProbability());
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
