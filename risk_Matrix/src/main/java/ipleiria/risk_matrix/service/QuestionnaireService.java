package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.exceptions.exception.QuestionnaireNotFoundException;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.repository.QuestionRepository;
import ipleiria.risk_matrix.repository.QuestionnaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
}
