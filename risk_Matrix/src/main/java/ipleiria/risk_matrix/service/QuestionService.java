package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.dto.QuestionOptionDTO;
import ipleiria.risk_matrix.exceptions.exception.InvalidCategoryException;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.exceptions.exception.QuestionNotFoundException;
import ipleiria.risk_matrix.exceptions.exception.QuestionnaireNotFoundException;
import ipleiria.risk_matrix.models.category.Category;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.repository.CategoryRepository;
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
    private final CategoryRepository categoryRepository;

    @Autowired
    public QuestionService(QuestionRepository questionRepository,
                           QuestionnaireRepository questionnaireRepository,
                           CategoryRepository categoryRepository) {
        this.questionRepository = questionRepository;
        this.questionnaireRepository = questionnaireRepository;
        this.categoryRepository = categoryRepository;
    }

    // Create a new question
    public QuestionDTO createQuestion(Long questionnaireId, QuestionDTO questionDTO) {
        // 1. Load the Questionnaire
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new QuestionnaireNotFoundException(
                        "Questionnaire not found for ID: " + questionnaireId
                ));

        // 2. Build a new Question entity
        Question question = new Question();
        question.setQuestionText(questionDTO.getQuestionText());

        // 3. Validate & set dynamic category using categoryName from DTO
        String categoryName = questionDTO.getCategoryName();
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new InvalidCategoryException("A categoria da pergunta é obrigatória.");
        }
        Category category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(categoryName);
                    return categoryRepository.save(newCategory);
                });
        question.setCategory(category);

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
            naoAplicavel.setOptionType(OptionLevelType.IMPACT);
            naoAplicavel.setOptionLevel(OptionLevel.LOW);
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
        option.setOptionType(optionDTO.getOptionType());
        option.setOptionLevel(optionDTO.getOptionLevel());
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

    // Get questions by category (using dynamic category name)
    public List<Question> getQuestionsByCategory(String categoryName) {
        return questionRepository.findAll().stream()
                .filter(q -> q.getCategory() != null &&
                        q.getCategory().getName().equalsIgnoreCase(categoryName))
                .collect(Collectors.toList());
    }

    // Get a question by ID
    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    // Delete a question
    public void deleteQuestion(Long id) {
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

        // Update dynamic category using categoryName from DTO
        String categoryName = updatedQuestionDTO.getCategoryName();
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new InvalidCategoryException("A categoria da pergunta é obrigatória.");
        }
        Category category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(categoryName);
                    return categoryRepository.save(newCategory);
                });
        existingQuestion.setCategory(category);

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
        option.setOptionType(dto.getOptionType());
        option.setOptionLevel(dto.getOptionLevel());
        option.setQuestion(question);
        return option;
    }
}
