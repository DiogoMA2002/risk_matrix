package ipleiria.risk_matrix.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ipleiria.risk_matrix.dto.QuestionnaireDTO;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.exceptions.exception.QuestionnaireNotFoundException;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionCategory;
import ipleiria.risk_matrix.repository.QuestionnaireRepository;
import ipleiria.risk_matrix.service.QuestionnaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/questionnaires")
public class QuestionnaireController {

    @Autowired
    private QuestionnaireService questionnaireService;
    @Autowired
    private QuestionnaireRepository questionnaireRepository;
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
    @GetMapping("/{questionnaireId}/category/{category}")
    public List<Question> getQuestionsByQuestionnaireAndCategory(
            @PathVariable Long questionnaireId,
            @PathVariable QuestionCategory category
    ) {
        // 1) Load the questionnaire
        Questionnaire q = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new QuestionnaireNotFoundException(
                        "Questionnaire not found for ID: " + questionnaireId
                ));

        // 2) Filter the questions in that questionnaire by the given category
        return q.getQuestions().stream()
                .filter(question -> question.getCategory() == category)
                .collect(Collectors.toList());
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

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportQuestionnaire(@PathVariable Long id) throws JsonProcessingException {
        Questionnaire questionnaire = questionnaireService.getQuestionnaireById(id)
                .orElseThrow(() -> new NotFoundException("Questionnaire not found for ID: " + id));

        // Convert to JSON
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(questionnaire);

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        // Add content-disposition manually
        headers.add("Content-Disposition", "attachment; filename=\"questionnaire_" + id + ".json\"");
        // Set content type
        headers.add("Content-Type", "application/json; charset=UTF-8");
        // Or: headers.setContentType(MediaType.APPLICATION_JSON) if your version supports it

        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @PostMapping("/import")
    public Questionnaire importQuestionnaire(@RequestBody Questionnaire incoming) {
        return questionnaireService.importQuestionnaire(incoming);
    }
    @PutMapping("/{id}")
    public Questionnaire updateQuestionnaire(@PathVariable Long id, @RequestBody Questionnaire updatedQuestionnaire) {
        return questionnaireService.updateQuestionnaire(id, updatedQuestionnaire);
    }

    @GetMapping("/{id}/questions")
    public List<Question> getAllQuestionsForQuestionnaire(@PathVariable Long id) {
        return questionnaireService.getAllQuestionsForQuestionnaire(id);
    }

    @GetMapping("/search")
    public List<QuestionnaireDTO> searchQuestionnaires(@RequestParam(required = false) String title) {
        return questionnaireService.searchQuestionnaires(title)
                .stream()
                .map(QuestionnaireDTO::new)
                .collect(Collectors.toList());
    }

}
