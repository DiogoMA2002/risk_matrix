package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.dto.UserAnswersDTO;
import ipleiria.risk_matrix.exceptions.exception.InvalidOptionException;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.models.questions.Severity;
import ipleiria.risk_matrix.repository.AnswerRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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

    public AnswerDTO submitAnswer(AnswerDTO answerDTO) {
        if (answerDTO.getEmail() == null || answerDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        Question question = questionRepository.findById(answerDTO.getQuestionId())
                .orElseThrow(() -> new NotFoundException("Question not found for ID: " + answerDTO.getQuestionId()));

        QuestionOption selectedOption = question.getOptions().stream()
                .filter(option -> option.getOptionText().equals(answerDTO.getUserResponse()))
                .findFirst()
                .orElseThrow(() -> new InvalidOptionException("Invalid response: " + answerDTO.getUserResponse()));

        Answer answer = new Answer();
        answer.setQuestionId(question.getId());
        answer.setQuestionText(question.getQuestionText());
        answer.setUserResponse(answerDTO.getUserResponse());
        answer.setEmail(answerDTO.getEmail());
        answer.setQuestionType(selectedOption.getOptionType());
        answer.setChosenLevel(selectedOption.getOptionLevel());
        answer.setSubmissionId(Optional.ofNullable(answerDTO.getSubmissionId())
                .orElse(UUID.randomUUID().toString()));

        answerRepository.save(answer);
        return new AnswerDTO(answer);
    }

    public List<AnswerDTO> getAnswersByEmail(String email) {
        return answerRepository.findByEmail(email).stream()
                .map(AnswerDTO::new)
                .collect(Collectors.toList());
    }

    public List<AnswerDTO> getAnswersByQuestion(Long questionId) {
        return answerRepository.findByQuestionId(questionId).stream()
                .map(AnswerDTO::new)
                .collect(Collectors.toList());
    }

    public List<AnswerDTO> getAllAnswers() {
        return answerRepository.findAll().stream()
                .map(AnswerDTO::new)
                .collect(Collectors.toList());
    }

    public List<AnswerDTO> submitMultipleAnswers(List<AnswerDTO> answers) {
        String submissionId = UUID.randomUUID().toString();
        return answers.stream()
                .map(ans -> {
                    ans.setSubmissionId(submissionId);
                    return submitAnswer(ans);
                }).toList();
    }

    public List<UserAnswersDTO> getUserSubmissionsWithSeverities(String email) {
        List<AnswerDTO> answers = getAnswersByEmail(email);
        return processSubmissions(answers);
    }

    public List<UserAnswersDTO> getAllSubmissionsWithSeverityAndEmail() {
        List<AnswerDTO> allAnswers = getAllAnswers();
        return processSubmissions(allAnswers);
    }

    public List<UserAnswersDTO> getAnswersByDateRange(String startDate, String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
        List<AnswerDTO> answersInRange = answerRepository.findByCreatedAtBetween(start, end).stream()
                .map(AnswerDTO::new)
                .toList();

        return processSubmissions(answersInRange);
    }

    private List<UserAnswersDTO> processSubmissions(List<AnswerDTO> answers) {
        Map<Long, Question> questionMap = loadQuestionsByAnswers(answers);
        Map<String, List<AnswerDTO>> groupedBySubmission = answers.stream()
                .collect(Collectors.groupingBy(AnswerDTO::getSubmissionId));

        List<UserAnswersDTO> result = new ArrayList<>();
        for (var entry : groupedBySubmission.entrySet()) {
            String submissionId = entry.getKey();
            List<AnswerDTO> submissionAnswers = entry.getValue();
            String email = submissionAnswers.getFirst().getEmail();

            Map<String, List<AnswerDTO>> groupedByCategory = groupAnswersByCategory(submissionAnswers, questionMap, submissionId);
            Map<String, Severity> severities = calculateSeverities(groupedByCategory);

            UserAnswersDTO dto = new UserAnswersDTO();
            dto.setSubmissionId(submissionId);
            dto.setEmail(email);
            dto.setAnswers(submissionAnswers);
            dto.setSeveritiesByCategory(severities);
            result.add(dto);
        }
        return result;
    }

    private Map<Long, Question> loadQuestionsByAnswers(List<AnswerDTO> answers) {
        Set<Long> questionIds = answers.stream()
                .map(AnswerDTO::getQuestionId)
                .collect(Collectors.toSet());
        return questionRepository.findAllById(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));
    }

    private Map<String, List<AnswerDTO>> groupAnswersByCategory(List<AnswerDTO> answers, Map<Long, Question> questionMap, String submissionId) {
        Map<String, List<AnswerDTO>> grouped = new HashMap<>();
        for (AnswerDTO ans : answers) {
            Question q = questionMap.get(ans.getQuestionId());
            if (q == null || q.getCategory() == null) {
                System.err.println("Missing question or category for ID: " + ans.getQuestionId() + " [Submission: " + submissionId + "]");
                continue;
            }
            String categoryName = q.getCategory().getName();
            grouped.computeIfAbsent(categoryName, _ -> new ArrayList<>()).add(ans);
        }
        return grouped;
    }

    private Map<String, Severity> calculateSeverities(Map<String, List<AnswerDTO>> groupedAnswers) {
        return groupedAnswers.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> computeCategorySeverity(e.getValue())));
    }
}
