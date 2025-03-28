package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.exceptions.exception.QuestionnaireNotFoundException;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.repository.QuestionRepository;
import ipleiria.risk_matrix.repository.QuestionnaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static ipleiria.risk_matrix.utils.QuestionUtils.ensureNaoAplicavelOption;

@Service
public class QuestionnaireService {

    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    @Autowired
    private QuestionRepository questionRepository;

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
    public Question addQuestionToQuestionnaire(Long questionnaireId, Question question) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new QuestionnaireNotFoundException(
                        "Questionário não encontrado para o ID: " + questionnaireId
                ));

        question.setQuestionnaire(questionnaire);
        return questionRepository.save(question);
    }

    public void deleteQuestionnaire(Long id) {
        // Check if questionnaire exists
        if (!questionnaireRepository.existsById(id)) {
            throw new QuestionnaireNotFoundException(
                    "Não foi possível excluir. Questionário não encontrado para o ID: " + id
            );
        }
        questionnaireRepository.deleteById(id);
    }

    public Questionnaire importQuestionnaire(Questionnaire incoming) {
        // 1. Nullify the top-level ID
        incoming.setId(null);

        // 2. For each question, nullify IDs, set references
        if (incoming.getQuestions() != null) {
            for (Question q : incoming.getQuestions()) {
                q.setId(null);
                q.setQuestionnaire(incoming);

                // 3. For each option, nullify IDs
                if (q.getOptions() != null) {
                    q.getOptions().forEach(opt -> opt.setId(null));
                }

                // 4. Ensure "Não Aplicável" is present
                ensureNaoAplicavelOption(q);
            }
        }

        // 5. Save
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
    // In QuestionnaireService.java
    public Questionnaire updateQuestionnaire(Long id, Questionnaire updatedQuestionnaire) {
        Questionnaire existing = questionnaireRepository.findById(id)
                .orElseThrow(() -> new QuestionnaireNotFoundException("Questionnaire not found for ID: " + id));
        existing.setTitle(updatedQuestionnaire.getTitle());
        return questionnaireRepository.save(existing);
    }

}
