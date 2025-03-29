package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.dto.QuestionOptionDTO;
import ipleiria.risk_matrix.exceptions.exception.InvalidCategoryException;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.exceptions.exception.QuestionNotFoundException;
import ipleiria.risk_matrix.exceptions.exception.QuestionnaireNotFoundException;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.*;
import ipleiria.risk_matrix.repository.QuestionRepository;
import ipleiria.risk_matrix.repository.QuestionnaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionnaireRepository questionnaireRepository;

    public QuestionService(QuestionRepository questionRepository, QuestionnaireRepository questionnaireRepository) {
        this.questionRepository = questionRepository;
        this.questionnaireRepository = questionnaireRepository;
    }

    // Create a new question
    public QuestionDTO createQuestion(Long questionnaireId, QuestionDTO questionDTO) {
        // 1. Load the Questionnaire
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new QuestionnaireNotFoundException(
                        "Questionnaire not found for ID: " + questionnaireId
                ));

        // 2. Build a brand-new Question entity
        Question question = new Question();
        question.setQuestionText(questionDTO.getQuestionText());

        // 3. Validate category
        try {
            question.setCategory(QuestionCategory.valueOf(questionDTO.getCategory()));
        } catch (IllegalArgumentException e) {
            throw new InvalidCategoryException("Invalid category: " + questionDTO.getCategory());
        }

        question.setQuestionnaire(questionnaire);

        // 4. Convert incoming DTO options to entities
        List<QuestionOption> options = questionDTO.getOptions().stream()
                .map(optionDTO -> mapToQuestionOption(optionDTO, question))
                .collect(Collectors.toList());

        // 5. Automatically add "Não Aplicável" if not present
        boolean hasNaoAplicavel = options.stream()
                .anyMatch(opt -> "Não Aplicável".equalsIgnoreCase(opt.getOptionText()));

        if (!hasNaoAplicavel) {
            QuestionOption naoAplicavel = new QuestionOption();
            naoAplicavel.setOptionText("Não Aplicável");
            naoAplicavel.setOptionType(OptionLevelType.IMPACT); // or whatever type
            naoAplicavel.setOptionLevel(OptionLevel.LOW);       // or any default
            naoAplicavel.setQuestion(question);
            options.add(naoAplicavel);
        }

        // 6. Attach options to the new question
        question.setOptions(options);

        // 7. Save the new question
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
    public QuestionDTO updateQuestion(Long id, QuestionDTO updatedQuestionDTO) {
        // Retrieve the existing question or throw an exception if not found.
        Question existingQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Question not found with id: " + id));

        // Update the question text.
        existingQuestion.setQuestionText(updatedQuestionDTO.getQuestionText());

        // Convert the string from the DTO to the enum.
        existingQuestion.setCategory(QuestionCategory.valueOf(updatedQuestionDTO.getCategory().toUpperCase()));

        // Clear current options and convert new ones from the DTO.
        existingQuestion.getOptions().clear();
        if (updatedQuestionDTO.getOptions() != null) {
            List<QuestionOption> newOptions = updatedQuestionDTO.getOptions()
                    .stream()
                    .map(dto -> convertDtoToQuestionOption(dto, existingQuestion))
                    .collect(Collectors.toList());
            existingQuestion.getOptions().addAll(newOptions);
        }

        // Save the updated question.
        Question savedQuestion = questionRepository.save(existingQuestion);

        // Return a DTO of the saved question.
        return new QuestionDTO(savedQuestion);
    }

    // Helper method to convert a QuestionOptionDTO into a QuestionOption entity.
    private QuestionOption convertDtoToQuestionOption(QuestionOptionDTO dto, Question question) {
        QuestionOption option = new QuestionOption();
        option.setOptionText(dto.getOptionText());
        option.setOptionType(dto.getOptionType()); // Already an enum of type OptionLevelType
        option.setOptionLevel(dto.getOptionLevel()); // Already an enum of type OptionLevel
        // If you need to set severity, do so here (if applicable)
        option.setQuestion(question); // Associate this option with the parent question
        return option;
    }
}
