package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.dto.UserAnswersDTO;
import ipleiria.risk_matrix.exceptions.exception.InvalidOptionException;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.*;
import ipleiria.risk_matrix.repository.AnswerRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ipleiria.risk_matrix.utils.RiskUtils.computeCategorySeverity;

@Service
public class AnswerService {

    private static final Logger logger = LoggerFactory.getLogger(AnswerService.class);
    private static final int MAX_PAGE_SIZE = 500;

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
                .orElseThrow(() -> {
                    String availableOptions = question.getOptions().stream()
                            .map(opt -> "'" + opt.getOptionText() + "'")
                            .collect(Collectors.joining(", "));
                    return new InvalidOptionException("Invalid response: '" + answerDTO.getUserResponse() +
                            "'. Available options for question " + answerDTO.getQuestionId() +
                            " are: " + availableOptions);
                });

        Answer answer = new Answer();
        answer.setQuestionId(question.getId());
        answer.setQuestionText(question.getQuestionText());
        answer.setUserResponse(answerDTO.getUserResponse());
        answer.setEmail(answerDTO.getEmail());
        answer.setQuestionType(selectedOption.getOptionType());
        answer.setChosenLevel(selectedOption.getOptionLevel());
        answer.setSubmissionId(Optional.ofNullable(answerDTO.getSubmissionId())
                .orElse(UUID.randomUUID().toString()));
        answer.setQuestionOptionId(selectedOption.getId());

        answerRepository.save(answer);
        return new AnswerDTO(answer);
    }

    public List<AnswerDTO> getAnswersByEmail(String email) {
        return answerRepository.findByEmail(email).stream()
                .map(AnswerDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<AnswerDTO> submitMultipleAnswers(List<AnswerDTO> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("A lista de respostas não pode estar vazia.");
        }

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

    public List<UserAnswersDTO> getAllSubmissionsWithSeverityAndEmail(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE));
        List<String> submissionIdsPage = answerRepository.findSubmissionIdsOrderByLatestAnswer(pageable).getContent();
        if (submissionIdsPage.isEmpty()) {
            return List.of();
        }

        List<AnswerDTO> allAnswers = answerRepository.findBySubmissionIdIn(submissionIdsPage).stream()
                .map(AnswerDTO::new)
                .toList();

        List<UserAnswersDTO> processed = processSubmissions(allAnswers);
        Map<String, Integer> pageOrder = new HashMap<>();
        for (int i = 0; i < submissionIdsPage.size(); i++) {
            pageOrder.put(submissionIdsPage.get(i), i);
        }

        processed.sort(Comparator.comparingInt(dto -> pageOrder.getOrDefault(dto.getSubmissionId(), Integer.MAX_VALUE)));
        return processed;
    }

    public List<UserAnswersDTO> getAnswersByDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must not be before start date.");
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end   = endDate.atTime(23, 59, 59);

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
            dto.setCreatedAt(submissionAnswers.getFirst().getCreatedAt());
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
                logger.warn("Missing question or category for ID: {} [Submission: {}]", ans.getQuestionId(), submissionId);
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

    @Transactional
    public void deleteSubmission(String submissionId) {
        List<Answer> answers = answerRepository.findBySubmissionId(submissionId);
        if (answers.isEmpty()) {
            throw new NotFoundException("No answers found for submission ID: " + submissionId);
        }
        answerRepository.deleteBySubmissionId(submissionId);
    }
}
