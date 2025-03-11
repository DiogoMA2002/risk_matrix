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

    // ✅ Create Answer based on input data
    public AnswerDTO createAnswer(Long questionId, AnswerDTO answerDTO) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setUserResponse(answerDTO.getUserResponse());
        answer.setImpact(answerDTO.getImpact());
        answer.setProbability(answerDTO.getProbability());

        // ✅ Calculate severity using the matrix logic
        Serverity severity = calculateSeverity(answer.getImpact(), answer.getProbability());
        answer.setServerity(severity);

        answerRepository.save(answer);

        // ✅ Add the answer to the question list
        question.getAnswers().add(answer);

        return new AnswerDTO(answer);
    }

    private Serverity calculateSeverity(Impact impact, Probability probability) {
        return switch (impact) {
            case HIGH -> switch (probability) {
                case HIGH -> Serverity.CRITICAL; // Special case for high-high = Critical
                case MEDIUM -> Serverity.HIGH;
                case LOW -> Serverity.MEDIUM;
            };
            case MEDIUM -> switch (probability) {
                case HIGH -> Serverity.HIGH;
                case MEDIUM -> Serverity.MEDIUM;
                case LOW -> Serverity.LOW;
            };
            case LOW -> switch (probability) {
                case HIGH -> Serverity.MEDIUM;
                case MEDIUM, LOW -> Serverity.LOW;
            };
        };
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
