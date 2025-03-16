package ipleiria.risk_matrix.controller;

import ipleiria.risk_matrix.dto.QuestionnaireDTO;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.service.QuestionnaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/questionnaires")
public class QuestionnaireController {

    @Autowired
    private QuestionnaireService questionnaireService;

    @PostMapping("/create")
    public Questionnaire createQuestionnaire(@RequestBody Questionnaire questionnaire) {
        return questionnaireService.createQuestionnaire(questionnaire);
    }

    @GetMapping("/all")
    public List<QuestionnaireDTO> getAllQuestionnaires() {
        return questionnaireService.getAllQuestionnaires().stream()
                .map(QuestionnaireDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Optional<Questionnaire> getQuestionnaireById(@PathVariable Long id) {
        return questionnaireService.getQuestionnaireById(id);
    }

    @PostMapping("/{id}/add-question")
    public Question addQuestionToQuestionnaire(@PathVariable Long id, @RequestBody Question question) {
        return questionnaireService.addQuestionToQuestionnaire(id, question);
    }
    // Deletar uma pergunta por ID
    @DeleteMapping("/delete/{id}")
    public void deleteQuestionnaire(@PathVariable Long id) {
        questionnaireService.deleteQuestionnaire(id);
    }

}
