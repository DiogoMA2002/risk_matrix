package ipleiria.risk_matrix.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.dto.QuestionnaireDTO;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.exceptions.exception.QuestionnaireNotFoundException;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.repository.QuestionnaireRepository;
import ipleiria.risk_matrix.service.QuestionnaireService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/questionnaires")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;
    private final QuestionnaireRepository questionnaireRepository;

    public QuestionnaireController(QuestionnaireService questionnaireService, QuestionnaireRepository questionnaireRepository) {
        this.questionnaireService = questionnaireService;
        this.questionnaireRepository = questionnaireRepository;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Questionnaire> createQuestionnaire(@RequestBody @Valid QuestionnaireDTO questionnaireDTO) {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setTitle(questionnaireDTO.getTitle());
        questionnaire.setQuestions(new ArrayList<>()); // Ensure non-null

        Questionnaire saved = questionnaireService.createQuestionnaire(questionnaire);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/all")
    public List<QuestionnaireDTO> getAllQuestionnaires() {
        return questionnaireService.getAllQuestionnaires().stream()
                .map(QuestionnaireDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Questionnaire> getQuestionnaireById(@PathVariable Long id) {
        return questionnaireService.getQuestionnaireById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new QuestionnaireNotFoundException("Questionnaire not found with id: " + id));
    }

    // Updated: Accept category name (String) and filter questions by dynamic category name
    @GetMapping("/{questionnaireId}/category/{categoryName}")
    public List<Question> getQuestionsByQuestionnaireAndCategory(
            @PathVariable Long questionnaireId,
            @PathVariable String categoryName
    ) {
        // 1) Load the questionnaire
        Questionnaire q = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new QuestionnaireNotFoundException(
                        "Questionnaire not found for ID: " + questionnaireId
                ));

        // 2) Filter the questions by checking the category name (case-insensitive)
        return q.getQuestions().stream()
                .filter(question -> question.getCategory() != null &&
                        question.getCategory().getName().equalsIgnoreCase(categoryName))
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/add-question")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Question> addQuestionToQuestionnaire(
            @PathVariable Long id,
            @RequestBody @Valid QuestionDTO dto) {
        Question added = questionnaireService.addQuestionDtoToQuestionnaire(id, dto);
        return ResponseEntity.ok(added);
    }

    // Delete a questionnaire by ID
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteQuestionnaire(@PathVariable Long id) {
        questionnaireService.deleteQuestionnaire(id);
    }

    @GetMapping("/{id}/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportQuestionnaire(@PathVariable Long id) throws JsonProcessingException {
        Questionnaire questionnaire = questionnaireService.getQuestionnaireById(id)
                .orElseThrow(() -> new NotFoundException("Questionnaire not found for ID: " + id));

        // Convert to JSON
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(questionnaire);

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=\"questionnaire_" + id + ".json\"");
        headers.add("Content-Type", "application/json; charset=UTF-8");

        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Questionnaire> importQuestionnaire(@RequestBody @Valid QuestionnaireDTO dto) {
        Questionnaire imported = questionnaireService.importQuestionnaireDto(dto);
        return ResponseEntity.ok(imported);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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
