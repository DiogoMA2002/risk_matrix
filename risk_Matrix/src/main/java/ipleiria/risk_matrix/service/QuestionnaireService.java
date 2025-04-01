package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.dto.QuestionOptionDTO;
import ipleiria.risk_matrix.dto.QuestionnaireDTO;
import ipleiria.risk_matrix.exceptions.exception.QuestionnaireNotFoundException;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionCategory;
import ipleiria.risk_matrix.models.questions.QuestionOption;
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

    public QuestionnaireService(QuestionnaireRepository questionnaireRepository, QuestionRepository questionRepository) {
        this.questionnaireRepository = questionnaireRepository;
        this.questionRepository = questionRepository;
    }

    // Criar um novo questionário
    public Questionnaire createQuestionnaire(Questionnaire questionnaire) {
        return questionnaireRepository.save(questionnaire);
    }

    // Obter todos os questionários
    public List<Questionnaire> getAllQuestionnaires() {
        return questionnaireRepository.findAll();
    }

    // Obter um questionário por ID
    public Optional<Questionnaire> getQuestionnaireById(Long id) {
        return questionnaireRepository.findById(id);
    }

    // Associar uma pergunta a um questionário
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

    // Importa um questionário com várias operações: nullifica IDs, associa referências, garante "Não Aplicável", etc.
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
                question.setCategory(questionDTO.getCategory()); // ✅ CORRECT
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

                ensureNaoAplicavelOption(question); // inject "Não Aplicável" if missing
                questionnaire.getQuestions().add(question);
            }
        }

        return questionnaireRepository.save(questionnaire);
    }

    @Transactional
    public Question addQuestionDtoToQuestionnaire(Long questionnaireId, @Valid QuestionDTO dto) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new QuestionnaireNotFoundException(
                        "Questionnaire not found for ID: " + questionnaireId
                ));

        Question question = new Question();
        question.setId(null);
        question.setQuestionText(dto.getQuestionText());
        question.setCategory(dto.getCategory()); // Already parsed
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
