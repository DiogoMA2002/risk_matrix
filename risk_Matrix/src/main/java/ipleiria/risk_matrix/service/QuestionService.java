package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.dto.QuestionOptionDTO;
import ipleiria.risk_matrix.exceptions.exception.InvalidCategoryException;
import ipleiria.risk_matrix.exceptions.exception.QuestionNotFoundException;
import ipleiria.risk_matrix.exceptions.exception.QuestionnaireNotFoundException;
import ipleiria.risk_matrix.exceptions.exception.handleDuplicateException;
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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final CategoryRepository categoryRepository;

    public QuestionService(QuestionRepository questionRepository,
                           QuestionnaireRepository questionnaireRepository,
                           CategoryRepository categoryRepository) {
        this.questionRepository = questionRepository;
        this.questionnaireRepository = questionnaireRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public QuestionDTO createQuestion(QuestionDTO questionDTO) {
        Objects.requireNonNull(questionDTO, "QuestionDTO must not be null");

        Question question = questionRepository.findByQuestionText(questionDTO.getQuestionText())
                .orElseGet(() -> buildNewQuestionFromDTO(questionDTO));

        associateQuestionWithQuestionnaires(question, questionDTO.getQuestionnaireIds());

        return new QuestionDTO(questionRepository.save(question));
    }

    private Question buildNewQuestionFromDTO(QuestionDTO dto) {
        Question question = new Question();
        question.setQuestionText(dto.getQuestionText());

        Category category = resolveOrCreateCategory(dto.getCategoryName());
        question.setCategory(category);

        List<QuestionOption> options = dto.getOptions() != null ? dto.getOptions().stream()
                .map(optDto -> buildOptionFromDTO(optDto, question))
                .collect(Collectors.toList()) : new ArrayList<>();

        ensureNaoAplicavelExists(options, question);
        question.setOptions(options);

        return questionRepository.save(question);
    }

    private QuestionOption buildOptionFromDTO(QuestionOptionDTO dto, Question question) {
        QuestionOption opt = new QuestionOption();
        opt.setOptionText(dto.getOptionText());
        opt.setOptionType(dto.getOptionType());
        opt.setOptionLevel(dto.getOptionLevel());
        opt.setQuestion(question);
        return opt;
    }

    private void ensureNaoAplicavelExists(List<QuestionOption> options, Question question) {
        boolean exists = options.stream()
                .anyMatch(opt -> "Não Aplicável".equalsIgnoreCase(opt.getOptionText()));

        if (!exists) {
            QuestionOption naoAplicavel = new QuestionOption();
            naoAplicavel.setOptionText("Não Aplicável");
            naoAplicavel.setOptionType(OptionLevelType.IMPACT);
            naoAplicavel.setOptionLevel(OptionLevel.LOW);
            naoAplicavel.setQuestion(question);
            options.add(naoAplicavel);
        }
    }

    private Category resolveOrCreateCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new InvalidCategoryException("A categoria da pergunta é obrigatória.");
        }
        return categoryRepository.findByName(categoryName.trim())
                .orElseGet(() -> {
                    Category newCat = new Category();
                    newCat.setName(categoryName.trim());
                    return categoryRepository.save(newCat);
                });
    }

    private void associateQuestionWithQuestionnaires(Question question, List<Long> questionnaireIds) {
        if (questionnaireIds == null) return;

        for (Long id : new HashSet<>(questionnaireIds)) {
            Questionnaire q = questionnaireRepository.findById(id)
                    .orElseThrow(() -> new QuestionnaireNotFoundException("Questionnaire not found for ID: " + id));

            if (!question.getQuestionnaires().contains(q)) {
                question.getQuestionnaires().add(q);
                q.getQuestions().add(question);
            }
        }
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public List<Question> getQuestionsByCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) return Collections.emptyList();

        return questionRepository.findAll().stream()
                .filter(q -> q.getCategory() != null &&
                        q.getCategory().getName().equalsIgnoreCase(categoryName))
                .collect(Collectors.toList());
    }

    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found with id: " + id));
    }

    @Transactional
    public void deleteQuestion(Long id) {
        Question question = getQuestionById(id);

        for (Questionnaire q : new ArrayList<>(question.getQuestionnaires())) {
            q.getQuestions().remove(question);
        }
        question.getQuestionnaires().clear();

        questionRepository.delete(question);
    }
    public QuestionDTO getQuestionDtoById(Long id) {
        Question question = getQuestionById(id);
        return new QuestionDTO(question);
    }

    @Transactional
    public QuestionDTO updateQuestion(Long id, QuestionDTO dto) {
        // Defensive: check for duplicates before making changes
        questionRepository.findByQuestionText(dto.getQuestionText())
                .filter(q -> !q.getId().equals(id)) // allow if it's the same question
                .ifPresent(q -> {
                    throw new handleDuplicateException("Já existe uma pergunta com esse texto.");
                });

        Question existing = getQuestionById(id);

        existing.setQuestionText(dto.getQuestionText());

        Category category = resolveOrCreateCategory(dto.getCategoryName());
        existing.setCategory(category);

        existing.getOptions().clear();
        if (dto.getOptions() != null) {
            existing.getOptions().addAll(dto.getOptions().stream()
                    .map(opt -> buildOptionFromDTO(opt, existing))
                    .toList());
        }

        QuestionUtils.ensureNaoAplicavelOption(existing);

        for (Questionnaire q : new ArrayList<>(existing.getQuestionnaires())) {
            q.getQuestions().remove(existing);
        }
        existing.getQuestionnaires().clear();

        associateQuestionWithQuestionnaires(existing, dto.getQuestionnaireIds());

        return new QuestionDTO(questionRepository.save(existing));
    }
}
