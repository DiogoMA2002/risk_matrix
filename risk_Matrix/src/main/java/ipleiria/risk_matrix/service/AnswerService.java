package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.dto.UserAnswersDTO;
import ipleiria.risk_matrix.exceptions.exception.InvalidOptionException;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.*;
import ipleiria.risk_matrix.repository.AnswerRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.function.Function;


import static ipleiria.risk_matrix.utils.RiskUtils.computeSeverity;
import static ipleiria.risk_matrix.utils.RiskUtils.medianLevel;

@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    public AnswerService(AnswerRepository answerRepository, QuestionRepository questionRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }


    // Create (submit) an answer
    public AnswerDTO submitAnswer(AnswerDTO answerDTO) {
        // 1. Find the question
        Question question = questionRepository.findById(answerDTO.getQuestionId())
                .orElseThrow(() -> new NotFoundException(
                        "Question not found for ID: " + answerDTO.getQuestionId()
                ));

        // 2. Find the matching option by userResponse text
        QuestionOption selectedOption = question.getOptions().stream()
                .filter(option -> option.getOptionText().equals(answerDTO.getUserResponse()))
                .findFirst()
                .orElseThrow(() -> new InvalidOptionException(
                        "Invalid response: " + answerDTO.getUserResponse()
                ));

        // 3. Build and save the Answer entity
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

    // Submit multiple answers at once
    public List<AnswerDTO> submitMultipleAnswers(List<AnswerDTO> answers) {
        List<AnswerDTO> result = new ArrayList<>();
        for (AnswerDTO answerDTO : answers) {
            // Reuse your single-answer logic
            AnswerDTO saved = submitAnswer(answerDTO);
            result.add(saved);
        }
        return result;
    }
    public UserAnswersDTO getUserAnswersWithSeverities(String email) {
        List<AnswerDTO> answers = getAnswersByEmail(email);

        // Batch load all questions for the answers
        Set<Long> questionIds = answers.stream()
                .map(AnswerDTO::getQuestionId)
                .collect(Collectors.toSet());
        List<Question> questions = questionRepository.findAllById(questionIds);

        // Build a map for quick lookup: question ID -> Question
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        Map<String, List<AnswerDTO>> answersByCategory = new HashMap<>();
        for (AnswerDTO ans : answers) {
            Question question = questionMap.get(ans.getQuestionId());
            if (question == null) {
                throw new NotFoundException("Question not found for ID: " + ans.getQuestionId());
            }
            String categoryName = question.getCategory().name();
            answersByCategory.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(ans);
        }

        Map<String, Severity> severitiesByCategory = new HashMap<>();
        for (Map.Entry<String, List<AnswerDTO>> entry : answersByCategory.entrySet()) {
            severitiesByCategory.put(entry.getKey(), computeCategorySeverity(entry.getValue()));
        }

        UserAnswersDTO dto = new UserAnswersDTO();
        dto.setEmail(email);
        dto.setAnswers(answers);
        dto.setSeveritiesByCategory(severitiesByCategory);

        return dto;
    }

    public List<UserAnswersDTO> getAllAnswersWithSeverityAndEmail() {
        List<AnswerDTO> allAnswers = getAllAnswers();

        // Batch load questions for all answers
        Set<Long> questionIds = allAnswers.stream()
                .map(AnswerDTO::getQuestionId)
                .collect(Collectors.toSet());
        List<Question> questions = questionRepository.findAllById(questionIds);

        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        // Group answers by user email
        Map<String, List<AnswerDTO>> answersGroupedByEmail = allAnswers.stream()
                .collect(Collectors.groupingBy(AnswerDTO::getEmail));
        List<UserAnswersDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<AnswerDTO>> entry : answersGroupedByEmail.entrySet()) {
            String email = entry.getKey();
            List<AnswerDTO> userAnswers = entry.getValue();
            Map<String, List<AnswerDTO>> answersByCategory = new HashMap<>();

            for (AnswerDTO ans : userAnswers) {
                Question question = questionMap.get(ans.getQuestionId());
                if (question == null) {
                    throw new NotFoundException("Question not found for ID: " + ans.getQuestionId());
                }
                String categoryName = question.getCategory().name();
                answersByCategory.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(ans);
            }

            Map<String, Severity> severitiesByCategory = new HashMap<>();
            for (Map.Entry<String, List<AnswerDTO>> catEntry : answersByCategory.entrySet()) {
                severitiesByCategory.put(catEntry.getKey(), computeCategorySeverity(catEntry.getValue()));
            }

            UserAnswersDTO dto = new UserAnswersDTO();
            dto.setEmail(email);
            dto.setAnswers(userAnswers);
            dto.setSeveritiesByCategory(severitiesByCategory);
            result.add(dto);
        }
        return result;
    }


    // Helper method in AnswerService (always filters out "Não Aplicável")
    private Severity computeCategorySeverity(List<AnswerDTO> answers) {
        List<AnswerDTO> filteredAnswers = answers.stream()
                .filter(a -> !"Não Aplicável".equalsIgnoreCase(a.getUserResponse()))
                .collect(Collectors.toList());

        if (filteredAnswers.isEmpty()) {
            return Severity.LOW;
        }

        // Compute median for IMPACT levels
        List<OptionLevel> impactLevels = filteredAnswers.stream()
                .filter(a -> a.getQuestionType() == OptionLevelType.IMPACT)
                .map(AnswerDTO::getChosenLevel)
                .collect(Collectors.toList());
        OptionLevel medianImpact = medianLevel(impactLevels);

        // Compute median for PROBABILITY levels
        List<OptionLevel> probabilityLevels = filteredAnswers.stream()
                .filter(a -> a.getQuestionType() == OptionLevelType.PROBABILITY)
                .map(AnswerDTO::getChosenLevel)
                .collect(Collectors.toList());
        OptionLevel medianProbability = medianLevel(probabilityLevels);

        return computeSeverity(medianImpact, medianProbability);
    }


}
