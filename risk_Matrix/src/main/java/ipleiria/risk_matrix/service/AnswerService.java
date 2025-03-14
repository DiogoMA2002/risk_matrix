package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.dto.UserAnswersDTO;
import ipleiria.risk_matrix.exceptions.exception.InvalidOptionException;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.*;
import ipleiria.risk_matrix.repository.AnswerRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    // Create (submit) an answer
    public AnswerDTO submitAnswer(AnswerDTO answerDTO) {
        // Find the question
        Question question = questionRepository.findById(answerDTO.getQuestionId())
                .orElseThrow(() -> new NotFoundException(
                        "Invalid response: " + answerDTO.getUserResponse()
                ));

        // Find the matching option by userResponse text
        QuestionOption selectedOption = question.getOptions().stream()
                .filter(option -> option.getOptionText().equals(answerDTO.getUserResponse()))
                .findFirst()
                .orElseThrow(() -> new InvalidOptionException(
                        "Invalid response: " + answerDTO.getUserResponse()
                ));

        // Build the Answer entity
        Answer answer = new Answer();
        answer.setQuestionId(question.getId());
        answer.setQuestionText(question.getQuestionText());
        answer.setUserResponse(answerDTO.getUserResponse());
        answer.setEmail(answerDTO.getEmail());

        // Instead of separate impact/probability, we store type & level
        answer.setQuestionType(selectedOption.getOptionType());    // IMPACT or PROBABILITY
        answer.setChosenLevel(selectedOption.getOptionLevel());    // LOW, MEDIUM, HIGH
        answerRepository.save(answer);
        return new AnswerDTO(answer);
    }

    // Get answers by email
    public List<AnswerDTO> getAnswersByEmail(String email) {
        return answerRepository.findByEmail(email).stream()
                .map(AnswerDTO::new)
                .collect(Collectors.toList());
    }

    // Get answers by question ID
    public List<AnswerDTO> getAnswersByQuestion(Long questionId) {
        return answerRepository.findByQuestionId(questionId).stream()
                .map(AnswerDTO::new)
                .collect(Collectors.toList());
    }

    // Get all answers
    public List<AnswerDTO> getAllAnswers() {
        return answerRepository.findAll().stream()
                .map(AnswerDTO::new)
                .collect(Collectors.toList());
    }
    public UserAnswersDTO getUserAnswersWithSeverities(String email) {
        // 1. Get all answers for this user
        List<AnswerDTO> answers = getAnswersByEmail(email);

        // 2. Group them by category
        //    (We load each question by questionId to find the category)
        Map<String, List<AnswerDTO>> answersByCategory = new HashMap<>();
        for (AnswerDTO ans : answers) {
            // Load the question to get its category
            Question question = questionRepository.findById(ans.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            String categoryName = question.getCategory().name();
            answersByCategory
                    .computeIfAbsent(categoryName, k -> new ArrayList<>())
                    .add(ans);
        }

        // 3. For each category, compute the final severity
        //    by finding median IMPACT and median PROBABILITY
        Map<String, Severity> severitiesByCategory = new HashMap<>();

        for (Map.Entry<String, List<AnswerDTO>> entry : answersByCategory.entrySet()) {
            String category = entry.getKey();
            List<AnswerDTO> catAnswers = entry.getValue();

            // Collect all IMPACT levels
            List<OptionLevel> impactLevels = catAnswers.stream()
                    .filter(a -> a.getQuestionType() == OptionLevelType.IMPACT)
                    .map(AnswerDTO::getChosenLevel)
                    .collect(Collectors.toList());
            OptionLevel medianImpact = medianLevel(impactLevels);

            // Collect all PROBABILITY levels
            List<OptionLevel> probabilityLevels = catAnswers.stream()
                    .filter(a -> a.getQuestionType() == OptionLevelType.PROBABILITY)
                    .map(AnswerDTO::getChosenLevel)
                    .collect(Collectors.toList());
            OptionLevel medianProbability = medianLevel(probabilityLevels);

            // Cross them in the risk matrix
            Severity finalSeverity = computeSeverity(medianImpact, medianProbability);

            severitiesByCategory.put(category, finalSeverity);
        }

        // 4. Build the UserAnswersDTO
        UserAnswersDTO dto = new UserAnswersDTO();
        dto.setEmail(email);
        dto.setAnswers(answers);
        dto.setSeveritiesByCategory(severitiesByCategory);

        return dto;
    }

    private OptionLevel medianLevel(List<OptionLevel> levels) {
        if (levels == null || levels.isEmpty()) {
            return null; // or handle "no data" scenario
        }

        // Convert LOW=1, MEDIUM=2, HIGH=3
        List<Integer> numeric = levels.stream()
                .map(this::levelToInt)
                .sorted()
                .collect(Collectors.toList());

        int size = numeric.size();
        if (size % 2 == 1) {
            // Odd
            return intToLevel(numeric.get(size / 2));
        } else {
            // Even
            int left = numeric.get(size / 2 - 1);
            int right = numeric.get(size / 2);
            int avg = (left + right) / 2;
            return intToLevel(avg);
        }
    }

    private int levelToInt(OptionLevel level) {
        return switch (level) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
        };
    }

    private OptionLevel intToLevel(int val) {
        return switch (val) {
            case 1 -> OptionLevel.LOW;
            case 2 -> OptionLevel.MEDIUM;
            case 3 -> OptionLevel.HIGH;
            default -> null;
        };
    }

    private Severity computeSeverity(OptionLevel impact, OptionLevel probability) {
        // If either is null, default to something
        if (impact == null || probability == null) {
            return Severity.LOW; // or handle differently
        }

        return switch (impact) {
            case HIGH -> switch (probability) {
                case HIGH -> Severity.CRITICAL;
                case MEDIUM -> Severity.HIGH;
                case LOW -> Severity.MEDIUM;
            };
            case MEDIUM -> switch (probability) {
                case HIGH -> Severity.HIGH;
                case MEDIUM -> Severity.MEDIUM;
                case LOW -> Severity.LOW;
            };
            case LOW -> switch (probability) {
                case HIGH -> Severity.MEDIUM;
                case MEDIUM, LOW -> Severity.LOW;
            };
        };
    }
}
