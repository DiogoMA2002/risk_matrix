package ipleiria.risk_matrix.service;
import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.answers.RiskLevel;
import ipleiria.risk_matrix.models.questions.Question;
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

    // Criar uma resposta
    public Answer submitAnswer(Long questionId, String userResponse) {
        Optional<Question> question = questionRepository.findById(questionId);
        if (question.isEmpty()) {
            throw new RuntimeException("Pergunta não encontrada!");
        }

        // Lógica para calcular o nível de risco
        RiskLevel calculatedRisk = calculateRiskLevel(userResponse);

        Answer answer = new Answer();
        answer.setQuestion(question.get());
        answer.setUserResponse(userResponse);
        answer.setCalculatedRisk(calculatedRisk);

        return answerRepository.save(answer);
    }

    public AnswerDTO addAnswerToQuestion(Long questionId, String answerText) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Answer answer = new Answer();
        answer.setUserResponse(answerText);
        answer.setQuestion(question);

        answerRepository.save(answer);
        return new AnswerDTO(answer);
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

    // Buscar respostas por nível de risco
    public List<Answer> getAnswersByRiskLevel(RiskLevel riskLevel) {
        return answerRepository.findByCalculatedRisk(riskLevel);
    }

    // Lógica para calcular risco (exemplo básico)
    private RiskLevel calculateRiskLevel(String response) {
        switch (response.toLowerCase()) {
            case "sim": return RiskLevel.LOW;
            case "parcialmente": return RiskLevel.MEDIUM;
            case "não": return RiskLevel.HIGH;
            default: return RiskLevel.CRITICAL;
        }
    }
}
