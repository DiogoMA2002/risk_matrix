package ipleiria.risk_matrix.service;
import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.answers.Impact;
import ipleiria.risk_matrix.models.answers.Probability;
import ipleiria.risk_matrix.models.answers.Serverity;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionCategory;
import ipleiria.risk_matrix.repository.AnswerRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    public AnswerDTO createAnswer(Long questionId, String userResponse) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setUserResponse(userResponse);

        // 🔥 Get total suggestions through answers
        int totalSuggestions = question.getAnswers().stream()
                .flatMap(a -> a.getSuggestions().stream())
                .collect(Collectors.toList()).size();

        Probability probability = calculateProbability(userResponse, totalSuggestions);
        Impact impact = calculateImpact(question.getCategory(), totalSuggestions);

        answer.setProbability(probability);
        answer.setImpact(impact);
        answer.setServerity(calculateSeverity(impact, probability));

        answerRepository.save(answer);

        // ✅ Add answer to question list
        question.getAnswers().add(answer);

        return new AnswerDTO(answer);
    }

    private Probability calculateProbability(String userResponse, int totalSuggestions) {
        if ("yes".equalsIgnoreCase(userResponse) && totalSuggestions == 0) {
            return Probability.LOW;
        } else if ("yes".equalsIgnoreCase(userResponse) && totalSuggestions > 0) {
            return Probability.MEDIUM;
        } else if ("no".equalsIgnoreCase(userResponse)) {
            return Probability.HIGH;
        } else if ("partially".equalsIgnoreCase(userResponse)) {
            return Probability.MEDIUM;
        }
        return Probability.LOW;
    }

    private Impact calculateImpact(QuestionCategory category, int totalSuggestions) {
        switch (category) {
            case Risco_de_Autenticacao:
            case Seguranca_de_Email:
            case Risco_da_Rede_Interna:
                return totalSuggestions > 0 ? Impact.HIGH : Impact.MEDIUM;
            case Risco_de_Plataforma_da_Empresa:
            case Risco_de_Infraestrutura_de_Informação_Externa:
            case Risco_de_Infraestrutura_de_Informação_Interna:
                return totalSuggestions > 0 ? Impact.MEDIUM : Impact.LOW;
            default:
                return Impact.LOW;
        }
    }

    private Serverity calculateSeverity(Impact impact, Probability probability) {
        if (impact == Impact.HIGH && probability == Probability.HIGH) {
            return Serverity.CRITICAL;
        } else if (impact == Impact.HIGH || probability == Probability.HIGH) {
            return Serverity.HIGH;
        } else if (impact == Impact.MEDIUM || probability == Probability.MEDIUM) {
            return Serverity.MEDIUM;
        }
        return Serverity.LOW;
    }



    public List<AnswerDTO> getAnswersByQuestion(Long questionId) {
        return answerRepository.findByQuestionId(questionId)
                .stream()
                .map(AnswerDTO::new)
                .collect(Collectors.toList());
    }

    // Buscar todas as respostas
    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }


}
