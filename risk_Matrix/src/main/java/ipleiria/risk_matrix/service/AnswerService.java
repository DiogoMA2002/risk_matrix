package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.dto.UserAnswersDTO;
import ipleiria.risk_matrix.exceptions.exception.InvalidOptionException;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.models.questions.Severity;
import ipleiria.risk_matrix.repository.AnswerRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ipleiria.risk_matrix.utils.RiskUtils.computeCategorySeverity;

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
        answer.setChosenLevel(selectedOption.getOptionLevel());      // LOW, MEDIUM, HIGH

        // The submissionId should be generated on the backend (e.g., UUID) if not provided by the client.
        // For example:
        if (answerDTO.getSubmissionId() == null) {
            answer.setSubmissionId(java.util.UUID.randomUUID().toString());
        } else {
            answer.setSubmissionId(answerDTO.getSubmissionId());
        }

        answerRepository.save(answer);
        return new AnswerDTO(answer);
    }

    // Get answers by email (returns all answers from the user regardless of submission)
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
        // Generate a unique submissionId for the entire session
        String submissionId = java.util.UUID.randomUUID().toString();
        for (AnswerDTO answerDTO : answers) {
            // Set the same submissionId for each answer in this submission
            answerDTO.setSubmissionId(submissionId);
            AnswerDTO saved = submitAnswer(answerDTO);
            result.add(saved);
        }
        return result;
    }

    // Get all submissions for a given user (grouped by submissionId)
    public List<UserAnswersDTO> getUserSubmissionsWithSeverities(String email) {
        List<AnswerDTO> answers = getAnswersByEmail(email);

        // Batch load all questions for the answers
        Set<Long> questionIds = answers.stream()
                .map(AnswerDTO::getQuestionId)
                .collect(Collectors.toSet());
        List<Question> questions = questionRepository.findAllById(questionIds);

        // Build a map for quick lookup: question ID -> Question
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        // Group answers by submissionId instead of email
        Map<String, List<AnswerDTO>> answersGroupedBySubmission = answers.stream()
                .collect(Collectors.groupingBy(AnswerDTO::getSubmissionId));

        List<UserAnswersDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<AnswerDTO>> entry : answersGroupedBySubmission.entrySet()) {
            String submissionId = entry.getKey();
            List<AnswerDTO> submissionAnswers = entry.getValue();

            // Group answers by category for severity calculations
            Map<String, List<AnswerDTO>> answersByCategory = new HashMap<>();
            for (AnswerDTO ans : submissionAnswers) {
                Question question = questionMap.get(ans.getQuestionId());
                if (question == null) {
                    throw new NotFoundException("Question not found for ID: " + ans.getQuestionId());
                }
                String categoryName = question.getCategory().getName();
                answersByCategory.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(ans);
            }

            Map<String, Severity> severitiesByCategory = new HashMap<>();
            for (Map.Entry<String, List<AnswerDTO>> catEntry : answersByCategory.entrySet()) {
                severitiesByCategory.put(catEntry.getKey(), computeCategorySeverity(catEntry.getValue()));
            }

            UserAnswersDTO dto = new UserAnswersDTO();
            // Assuming you have added a submissionId field to UserAnswersDTO:
            dto.setSubmissionId(submissionId);
            dto.setEmail(email);
            dto.setAnswers(submissionAnswers);
            dto.setSeveritiesByCategory(severitiesByCategory);
            result.add(dto);
        }
        return result;
    }

    // Get all submissions grouped by submissionId (across all users)
    public List<UserAnswersDTO> getAllSubmissionsWithSeverityAndEmail() {
        List<AnswerDTO> allAnswers = getAllAnswers();

        // Batch load questions for all answers
        Set<Long> questionIds = allAnswers.stream()
                .map(AnswerDTO::getQuestionId)
                .collect(Collectors.toSet());
        List<Question> questions = questionRepository.findAllById(questionIds);

        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        // Group answers by submissionId instead of email
        Map<String, List<AnswerDTO>> answersGroupedBySubmission = allAnswers.stream()
                .collect(Collectors.groupingBy(AnswerDTO::getSubmissionId));

        List<UserAnswersDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<AnswerDTO>> entry : answersGroupedBySubmission.entrySet()) {
            String submissionId = entry.getKey();
            List<AnswerDTO> submissionAnswers = entry.getValue();
            // It is assumed that all answers in a submission share the same email.
            String email = submissionAnswers.get(0).getEmail();

            Map<String, List<AnswerDTO>> answersByCategory = new HashMap<>();
            for (AnswerDTO ans : submissionAnswers) {
                Question question = questionMap.get(ans.getQuestionId());
                if (question == null) {
                    throw new NotFoundException("Question not found for ID: " + ans.getQuestionId());
                }
                String categoryName = question.getCategory().getName();
                answersByCategory.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(ans);
            }

            Map<String, Severity> severitiesByCategory = new HashMap<>();
            for (Map.Entry<String, List<AnswerDTO>> catEntry : answersByCategory.entrySet()) {
                severitiesByCategory.put(catEntry.getKey(), computeCategorySeverity(catEntry.getValue()));
            }

            UserAnswersDTO dto = new UserAnswersDTO();
            dto.setSubmissionId(submissionId);
            dto.setEmail(email);
            dto.setAnswers(submissionAnswers);
            dto.setSeveritiesByCategory(severitiesByCategory);
            result.add(dto);
        }
        return result;
    }



    public List<AnswerDTO> getAnswersByDateRange(String startDate, String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
        return answerRepository.findByCreatedAtBetween(start, end).stream()
                .map(AnswerDTO::new)
                .collect(Collectors.toList());
    }



}
