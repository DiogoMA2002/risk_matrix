
package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.dto.QuestionOptionDTO;
import ipleiria.risk_matrix.dto.QuestionnaireDTO;
import ipleiria.risk_matrix.exceptions.exception.QuestionnaireNotFoundException;
import ipleiria.risk_matrix.models.category.Category;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.repository.CategoryRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import ipleiria.risk_matrix.repository.QuestionnaireRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ipleiria.risk_matrix.utils.QuestionUtils.ensureNaoAplicavelOption;

@Service
public class QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;

    public QuestionnaireService(QuestionnaireRepository questionnaireRepository,
                                QuestionRepository questionRepository,
                                CategoryRepository categoryRepository) {
        this.questionnaireRepository = questionnaireRepository;
        this.questionRepository = questionRepository;
        this.categoryRepository = categoryRepository;
    }

    public Questionnaire createQuestionnaire(Questionnaire questionnaire) {
        return questionnaireRepository.save(questionnaire);
    }

    public List<Questionnaire> getAllQuestionnaires() {
        return questionnaireRepository.findAll();
    }

    public Optional<Questionnaire> getQuestionnaireById(Long id) {
        return questionnaireRepository.findById(id);
    }

    public void deleteQuestionnaire(Long id) {
        if (!questionnaireRepository.existsById(id)) {
            throw new QuestionnaireNotFoundException("Questionnaire not found for ID: " + id);
        }
        questionnaireRepository.deleteById(id);
    }

    public List<Question> getAllQuestionsForQuestionnaire(Long id) {
        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new QuestionnaireNotFoundException("Questionnaire not found for ID: " + id));
        return questionnaire.getQuestions();
    }

    public List<Questionnaire> searchQuestionnaires(String title) {
        if (title == null || title.trim().isEmpty()) {
            return questionnaireRepository.findAll();
        }
        return questionnaireRepository.findByTitleContainingIgnoreCase(title);
    }

    public Questionnaire updateQuestionnaire(Long id, Questionnaire updatedQuestionnaire) {
        Questionnaire existing = questionnaireRepository.findById(id)
                .orElseThrow(() -> new QuestionnaireNotFoundException("Questionnaire not found for ID: " + id));
        existing.setTitle(updatedQuestionnaire.getTitle());
        return questionnaireRepository.save(existing);
    }

    @Transactional
    public Questionnaire importQuestionnaireDto(QuestionnaireDTO dto) {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setTitle(dto.getTitle());
        List<Question> resolvedQuestions = new ArrayList<>();

        for (QuestionDTO qdto : dto.getQuestions()) {
            Category category = resolveCategoryByName(qdto.getCategoryName());
            String questionText = validateQuestionText(qdto.getQuestionText());

            Optional<Question> existing = questionRepository.findByQuestionText(questionText);
            if (existing.isPresent()) {
                resolvedQuestions.add(existing.get());
                continue;
            }

            Question question = new Question();
            question.setQuestionText(questionText);
            question.setCategory(category);

            List<QuestionOption> options = mapOptionsFromDTOs(qdto.getOptions(), question);
            question.setOptions(options);

            ensureNaoAplicavelOption(question);

            resolvedQuestions.add(questionRepository.save(question));
        }

        questionnaire.setQuestions(resolvedQuestions);
        return questionnaireRepository.save(questionnaire);
    }


    @Transactional
    public Question addQuestionDtoToQuestionnaire(Long questionnaireId, @Valid QuestionDTO dto) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new QuestionnaireNotFoundException("Questionnaire not found for ID: " + questionnaireId));

        Question question = new Question();
        question.setQuestionText(validateQuestionText(dto.getQuestionText()));
        question.setCategory(resolveCategoryByName(dto.getCategoryName()));
        question.getQuestionnaires().add(questionnaire);
        questionnaire.getQuestions().add(question);

        if (dto.getOptions() != null) {
            List<QuestionOption> options = mapOptionsFromDTOs(dto.getOptions(), question);
            question.getOptions().addAll(options);
        }

        ensureNaoAplicavelOption(question);
        return questionRepository.save(question);
    }

    private Category resolveCategoryByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        return categoryRepository.findByName(name.trim())
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(name.trim());
                    return categoryRepository.save(newCategory);
                });
    }

    private String validateQuestionText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Question text cannot be empty.");
        }
        return text.trim();
    }

    private List<QuestionOption> mapOptionsFromDTOs(List<QuestionOptionDTO> optionDTOs, Question question) {
        if (optionDTOs == null || optionDTOs.isEmpty()) {
            throw new IllegalArgumentException("Question must have at least one option.");
        }
        return optionDTOs.stream().map(dto -> {
            if (dto.getOptionText() == null || dto.getOptionText().trim().isEmpty()) {
                throw new IllegalArgumentException("Option text cannot be blank.");
            }
            if (dto.getOptionType() == null || dto.getOptionLevel() == null) {
                throw new IllegalArgumentException("Option type and level must be specified.");
            }
            QuestionOption opt = new QuestionOption();
            opt.setOptionText(dto.getOptionText());
            opt.setOptionType(dto.getOptionType());
            opt.setOptionLevel(dto.getOptionLevel());
            opt.setQuestion(question);
            return opt;
        }).collect(Collectors.toList());
    }
}
