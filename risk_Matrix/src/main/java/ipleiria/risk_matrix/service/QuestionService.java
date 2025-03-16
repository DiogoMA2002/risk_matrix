package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.dto.QuestionOptionDTO;
import ipleiria.risk_matrix.exceptions.exception.InvalidCategoryException;
import ipleiria.risk_matrix.exceptions.exception.QuestionNotFoundException;
import ipleiria.risk_matrix.exceptions.exception.QuestionnaireNotFoundException;
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

    // Create a new question
    public QuestionDTO createQuestion(Long questionnaireId, QuestionDTO questionDTO) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new QuestionnaireNotFoundException(
                        "Questionnaire not found for ID: " + questionnaireId
                ));

        // Build Question entity
        Question question = new Question();
        question.setQuestionText(questionDTO.getQuestionText());

        // Validate category
        try {
            question.setCategory(QuestionCategory.valueOf(questionDTO.getCategory()));
        } catch (IllegalArgumentException e) {
            throw new InvalidCategoryException("Invalid category: " + questionDTO.getCategory());
        }

        question.setQuestionnaire(questionnaire);

        // Convert incoming DTO options to entities
        List<QuestionOption> options = questionDTO.getOptions().stream()
                .map(optionDTO -> mapToQuestionOption(optionDTO, question))
                .collect(Collectors.toList());

        question.setOptions(options);
        questionRepository.save(question);

        return new QuestionDTO(question);
    }

    private QuestionOption mapToQuestionOption(QuestionOptionDTO optionDTO, Question question) {
        QuestionOption option = new QuestionOption();
        option.setQuestion(question);
        option.setOptionText(optionDTO.getOptionText());
        // Instead of separate impact/probability:
        option.setOptionType(optionDTO.getOptionType());    // IMPACT or PROBABILITY
        option.setOptionLevel(optionDTO.getOptionLevel());  // LOW, MEDIUM, HIGH
        return option;
    }

    // Get all questions
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    // Get all questions (DTO version)
    public List<QuestionDTO> getAllQuestionsWithSuggestion() {
        return questionRepository.findAll()
                .stream()
                .map(QuestionDTO::new)
                .collect(Collectors.toList());
    }

    // Get questions by category
    public List<Question> getQuestionsByCategory(QuestionCategory category) {
        return questionRepository.findByCategory(category);
    }

    // Get a question by ID
    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    // Delete a question
    public void deleteQuestion(Long id) {
        // Optionally, you can check if it exists first:
        if (!questionRepository.existsById(id)) {
            throw new QuestionNotFoundException("Question not found for ID: " + id);
        }
        questionRepository.deleteById(id);
    }
}
