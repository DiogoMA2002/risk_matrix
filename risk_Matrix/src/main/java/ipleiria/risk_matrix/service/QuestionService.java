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
import ipleiria.risk_matrix.utils.QuestionUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    @Transactional
    public QuestionDTO createQuestion(QuestionDTO questionDTO) {
        // 1. Check if the question already exists by text.
        Optional<Question> existingOpt = questionRepository.findByQuestionText(questionDTO.getQuestionText());
        Question question;

        if (existingOpt.isPresent()) {
            question = existingOpt.get();
        } else {
            // 2. Create new Question
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

            // 4. Convert and attach options
            final Question finalQuestion = question;
            List<QuestionOption> options = questionDTO.getOptions().stream()
                    .map(optDto -> {
                        QuestionOption opt = new QuestionOption();
                        opt.setOptionText(optDto.getOptionText());
                        opt.setOptionType(optDto.getOptionType());
                        opt.setOptionLevel(optDto.getOptionLevel());
                        opt.setQuestion(finalQuestion);
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

            // Save the new question along with options.
            question = questionRepository.save(question);
        }

        // 6. Associate with all provided questionnaires.
        for (Long questionnaireId : questionDTO.getQuestionnaireIds()) {
            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                    .orElseThrow(() -> new QuestionnaireNotFoundException("Questionnaire not found for ID: " + questionnaireId));

            // Avoid duplicate associations by checking using proper entity equality.
            if (!question.getQuestionnaires().contains(questionnaire)) {
                question.getQuestionnaires().add(questionnaire);
                questionnaire.getQuestions().add(question);
            }
        }

        // Save final updates in one go.
        question = questionRepository.save(question);

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
    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found with id: " + id));
    }

    // Delete a question
    @Transactional
    public void deleteQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found for ID: " + id));

        for (Questionnaire q : new ArrayList<>(question.getQuestionnaires())) {
            q.getQuestions().remove(question);
        }
        question.getQuestionnaires().clear();

        questionRepository.delete(question);
    }


    public QuestionDTO updateQuestion(Long id, QuestionDTO updatedQuestionDTO) {
        Question existingQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Question not found with id: " + id));

        // Update question text
        existingQuestion.setQuestionText(updatedQuestionDTO.getQuestionText());

        // Handle category (create if not exists)
        String categoryName = Optional.ofNullable(updatedQuestionDTO.getCategoryName())
                .map(String::trim)
                .orElseThrow(() -> new InvalidCategoryException("A categoria da pergunta é obrigatória."));

        Category category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(categoryName);
                    return categoryRepository.save(newCategory);
                });
        existingQuestion.setCategory(category);

        // Replace options
        existingQuestion.getOptions().clear();
        if (updatedQuestionDTO.getOptions() != null) {
            List<QuestionOption> newOptions = updatedQuestionDTO.getOptions()
                    .stream()
                    .map(dto -> convertDtoToQuestionOption(dto, existingQuestion))
                    .toList();
            existingQuestion.getOptions().addAll(newOptions);
        }

        // Ensure "Não Aplicável" option exists
        QuestionUtils.ensureNaoAplicavelOption(existingQuestion);

        // Remove old questionnaire associations (both sides)
        for (Questionnaire q : new ArrayList<>(existingQuestion.getQuestionnaires())) {
            q.getQuestions().remove(existingQuestion);
        }
        existingQuestion.getQuestionnaires().clear();

        // Add new associations (avoiding duplicates)
        List<Long> distinctQuestionnaireIds = updatedQuestionDTO.getQuestionnaireIds().stream()
                .distinct()
                .toList();

        for (Long questionnaireId : distinctQuestionnaireIds) {
            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                    .orElseThrow(() -> new QuestionnaireNotFoundException("Questionnaire not found for ID: " + questionnaireId));

            if (!questionnaire.getQuestions().contains(existingQuestion)) {
                questionnaire.getQuestions().add(existingQuestion);
            }
            existingQuestion.getQuestionnaires().add(questionnaire);
        }

        // Save and return
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
