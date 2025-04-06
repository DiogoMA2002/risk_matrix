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

    // Create a new question and associate it with a questionnaire (many-to-many)
    public QuestionDTO createQuestion(Long questionnaireId, QuestionDTO questionDTO) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new QuestionnaireNotFoundException(
                        "Questionnaire not found for ID: " + questionnaireId));

        // 1. Check if question already exists by text
        Optional<Question> existing = questionRepository.findByQuestionText(questionDTO.getQuestionText());
        Question question;

        if (existing.isPresent()) {
            question = existing.get();
        } else {
            // 2. New Question
            question = new Question();
            question.setQuestionText(questionDTO.getQuestionText());

            // 3. Handle category
            String categoryName = questionDTO.getCategoryName();
            if (categoryName == null || categoryName.trim().isEmpty()) {
                throw new InvalidCategoryException("A categoria da pergunta é obrigatória.");
            }

            Category category = categoryRepository.findByName(categoryName.trim())
                    .orElseGet(() -> {
                        Category newCategory = new Category();
                        newCategory.setName(categoryName.trim());
                        return categoryRepository.save(newCategory);
                    });

            question.setCategory(category);
            final Question finalQuestion = question;
            // 4. Convert and attach options
            List<QuestionOption> options = questionDTO.getOptions().stream()
                    .map(optDto -> {
                        QuestionOption opt = new QuestionOption();
                        opt.setOptionText(optDto.getOptionText());
                        opt.setOptionType(optDto.getOptionType());
                        opt.setOptionLevel(optDto.getOptionLevel());
                        opt.setQuestion(finalQuestion); // assign parent
                        return opt;
                    }).collect(Collectors.toList());

            // 5. Ensure "Não Aplicável" exists
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

            question.setOptions(options);

            // ✅ Save the full question with options
            question = questionRepository.save(question);
        }

        // 6. Associate with questionnaire if needed
        if (!question.getQuestionnaires().contains(questionnaire)) {
            question.getQuestionnaires().add(questionnaire);
            questionnaire.getQuestions().add(question);
            question = questionRepository.save(question); // update join
        }

        return new QuestionDTO(question);
    }

    // Get all questions
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
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
