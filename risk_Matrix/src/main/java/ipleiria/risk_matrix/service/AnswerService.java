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

import static ipleiria.risk_matrix.utils.RiskUtils.computeSeverity;
import static ipleiria.risk_matrix.utils.RiskUtils.medianLevel;

@Service
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

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

    // Get answers by email + compute severities
    public UserAnswersDTO getUserAnswersWithSeverities(String email) {
        List<AnswerDTO> answers = getAnswersByEmail(email);

        // Group them by category
        Map<String, List<AnswerDTO>> answersByCategory = new HashMap<>();
        for (AnswerDTO ans : answers) {
            Question question = questionRepository.findById(ans.getQuestionId())
                    .orElseThrow(() -> new NotFoundException(
                            "Question not found for ID: " + ans.getQuestionId()
                    ));

            String categoryName = question.getCategory().name();
            answersByCategory
                    .computeIfAbsent(categoryName, k -> new ArrayList<>())
                    .add(ans);
        }

        // For each category, compute final severity
        Map<String, Severity> severitiesByCategory = new HashMap<>();

        for (Map.Entry<String, List<AnswerDTO>> entry : answersByCategory.entrySet()) {
            String category = entry.getKey();
            // Filter out "Não Aplicável" answers
            List<AnswerDTO> catAnswers = entry.getValue().stream()
                    .filter(a -> ! "Não Aplicável".equalsIgnoreCase(a.getUserResponse()))
                    .collect(Collectors.toList());

            // If all answers for this category were "Não Aplicável", catAnswers is empty
            // We can skip or default severity to LOW
            if (catAnswers.isEmpty()) {
                severitiesByCategory.put(category, Severity.LOW);
                continue;
            }

            // Collect IMPACT levels
            List<OptionLevel> impactLevels = catAnswers.stream()
                    .filter(a -> a.getQuestionType() == OptionLevelType.IMPACT)
                    .map(AnswerDTO::getChosenLevel)
                    .collect(Collectors.toList());
            OptionLevel medianImpact = medianLevel(impactLevels);

            // Collect PROBABILITY levels
            List<OptionLevel> probabilityLevels = catAnswers.stream()
                    .filter(a -> a.getQuestionType() == OptionLevelType.PROBABILITY)
                    .map(AnswerDTO::getChosenLevel)
                    .collect(Collectors.toList());
            OptionLevel medianProbability = medianLevel(probabilityLevels);

            Severity finalSeverity = computeSeverity(medianImpact, medianProbability);
            severitiesByCategory.put(category, finalSeverity);
        }

        // Build the UserAnswersDTO
        UserAnswersDTO dto = new UserAnswersDTO();
        dto.setEmail(email);
        dto.setAnswers(answers);
        dto.setSeveritiesByCategory(severitiesByCategory);

        return dto;
    }


    // Get all answers for all users + compute severities
    public List<UserAnswersDTO> getAllAnswersWithSeverityAndEmail() {
        // 1. Get all answers
        List<AnswerDTO> allAnswers = getAllAnswers();

        // 2. Group by user email
        Map<String, List<AnswerDTO>> answersGroupedByEmail = allAnswers.stream()
                .collect(Collectors.groupingBy(AnswerDTO::getEmail));

        List<UserAnswersDTO> result = new ArrayList<>();

        // 3. For each user, group by category + compute severity
        for (Map.Entry<String, List<AnswerDTO>> entry : answersGroupedByEmail.entrySet()) {
            String email = entry.getKey();
            List<AnswerDTO> userAnswers = entry.getValue();

            Map<String, List<AnswerDTO>> answersByCategory = new HashMap<>();
            for (AnswerDTO ans : userAnswers) {
                Question question = questionRepository.findById(ans.getQuestionId())
                        .orElseThrow(() -> new NotFoundException(
                                "Question not found for ID: " + ans.getQuestionId()
                        ));
                String categoryName = question.getCategory().name();
                answersByCategory.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(ans);
            }

            Map<String, Severity> severitiesByCategory = new HashMap<>();
            for (Map.Entry<String, List<AnswerDTO>> catEntry : answersByCategory.entrySet()) {
                String category = catEntry.getKey();
                List<AnswerDTO> catAnswers = catEntry.getValue();

                // Collect and compute median IMPACT
                List<OptionLevel> impactLevels = catAnswers.stream()
                        .filter(a -> a.getQuestionType() == OptionLevelType.IMPACT)
                        .map(AnswerDTO::getChosenLevel)
                        .collect(Collectors.toList());
                OptionLevel medianImpact = medianLevel(impactLevels);

                // Collect and compute median PROBABILITY
                List<OptionLevel> probabilityLevels = catAnswers.stream()
                        .filter(a -> a.getQuestionType() == OptionLevelType.PROBABILITY)
                        .map(AnswerDTO::getChosenLevel)
                        .collect(Collectors.toList());
                OptionLevel medianProbability = medianLevel(probabilityLevels);

                Severity finalSeverity = computeSeverity(medianImpact, medianProbability);
                severitiesByCategory.put(category, finalSeverity);
            }

            // Build the DTO for this email
            UserAnswersDTO dto = new UserAnswersDTO();
            dto.setEmail(email);
            dto.setAnswers(userAnswers);
            dto.setSeveritiesByCategory(severitiesByCategory);
            result.add(dto);
        }

        return result;
    }



}
