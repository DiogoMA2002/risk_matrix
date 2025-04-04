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

import java.util.List;
import java.util.Optional;

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

    // Create a new questionnaire
    public Questionnaire createQuestionnaire(Questionnaire questionnaire) {
        return questionnaireRepository.save(questionnaire);
    }

    // Get all questionnaires
    public List<Questionnaire> getAllQuestionnaires() {
        return questionnaireRepository.findAll();
    }

    // Get a questionnaire by ID
    public Optional<Questionnaire> getQuestionnaireById(Long id) {
        return questionnaireRepository.findById(id);
    }

    // Associate a question to a questionnaire
    @Transactional
    public Question addQuestionToQuestionnaire(Long questionnaireId, Question question) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new QuestionnaireNotFoundException(
                        "Questionário não encontrado para o ID: " + questionnaireId
                ));

        question.setQuestionnaire(questionnaire);
        return questionRepository.save(question);
    }

    public void deleteQuestionnaire(Long id) {
        if (!questionnaireRepository.existsById(id)) {
            throw new QuestionnaireNotFoundException(
                    "Não foi possível excluir. Questionário não encontrado para o ID: " + id
            );
        }
        questionnaireRepository.deleteById(id);
    }

    // Import a questionnaire (using the entity model directly)
    @Transactional
    public Questionnaire importQuestionnaire(Questionnaire incoming) {
        incoming.setId(null);

        if (incoming.getQuestions() != null) {
            for (Question q : incoming.getQuestions()) {
                q.setId(null);
                q.setQuestionnaire(incoming);

                if (q.getOptions() != null) {
                    q.getOptions().forEach(opt -> opt.setId(null));
                }
                ensureNaoAplicavelOption(q);
            }
        }

        return questionnaireRepository.save(incoming);
    }

    public List<Question> getAllQuestionsForQuestionnaire(Long id) {
        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new QuestionnaireNotFoundException("Questionnaire not found for ID: " + id));
        return questionnaire.getQuestions();
    }

    public List<Questionnaire> searchQuestionnaires(String title) {
        if (title == null || title.isEmpty()) {
            return questionnaireRepository.findAll();
        } else {
            return questionnaireRepository.findByTitleContainingIgnoreCase(title);
        }
    }

    public Questionnaire updateQuestionnaire(Long id, Questionnaire updatedQuestionnaire) {
        Questionnaire existing = questionnaireRepository.findById(id)
                .orElseThrow(() -> new QuestionnaireNotFoundException("Questionnaire not found for ID: " + id));
        existing.setTitle(updatedQuestionnaire.getTitle());
        return questionnaireRepository.save(existing);
    }

    // Import a questionnaire from a DTO
    @Transactional
    public Questionnaire importQuestionnaireDto(@Valid QuestionnaireDTO dto) {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setId(null);
        questionnaire.setTitle(dto.getTitle());

        if (dto.getQuestions() != null) {
            for (QuestionDTO questionDTO : dto.getQuestions()) {
                Question question = new Question();
                question.setId(null);
                question.setQuestionText(questionDTO.getQuestionText());

                // Lookup dynamic category by name using the DTO's categoryName field
                Category category = categoryRepository.findByName(questionDTO.getCategoryName())
                        .orElseGet(() -> {
                            Category newCategory = new Category();
                            newCategory.setName(questionDTO.getCategoryName());
                            return categoryRepository.save(newCategory);
                        });
                question.setCategory(category);

                question.setQuestionnaire(questionnaire);

                if (questionDTO.getOptions() != null) {
                    for (QuestionOptionDTO optionDTO : questionDTO.getOptions()) {
                        QuestionOption option = new QuestionOption();
                        option.setId(null);
                        option.setOptionText(optionDTO.getOptionText());
                        option.setOptionLevel(optionDTO.getOptionLevel());
                        option.setOptionType(optionDTO.getOptionType());
                        option.setQuestion(question);
                        question.getOptions().add(option);
                    }
                }

                ensureNaoAplicavelOption(question); // Ensure "Não Aplicável" option exists if needed
                questionnaire.getQuestions().add(question);
            }
        }

        return questionnaireRepository.save(questionnaire);
    }

    // Add a question to an existing questionnaire using a DTO
    @Transactional
    public Question addQuestionDtoToQuestionnaire(Long questionnaireId, @Valid QuestionDTO dto) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new QuestionnaireNotFoundException(
                        "Questionnaire not found for ID: " + questionnaireId
                ));

        Question question = new Question();
        question.setId(null);
        question.setQuestionText(dto.getQuestionText());

        // Lookup dynamic category by name from the DTO
        Category category = categoryRepository.findByName(dto.getCategoryName())
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(dto.getCategoryName());
                    return categoryRepository.save(newCategory);
                });
        question.setCategory(category);

        question.setQuestionnaire(questionnaire);

        if (dto.getOptions() != null) {
            for (QuestionOptionDTO optionDTO : dto.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setId(null);
                option.setOptionText(optionDTO.getOptionText());
                option.setOptionLevel(optionDTO.getOptionLevel());
                option.setOptionType(optionDTO.getOptionType());
                option.setQuestion(question);
                question.getOptions().add(option);
            }
        }

        ensureNaoAplicavelOption(question);
        return questionRepository.save(question);
    }
}
