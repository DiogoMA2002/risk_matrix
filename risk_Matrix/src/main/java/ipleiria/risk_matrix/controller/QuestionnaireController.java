package ipleiria.risk_matrix.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.dto.QuestionnaireDTO;
import ipleiria.risk_matrix.dto.QuestionUserDTO;
import ipleiria.risk_matrix.dto.QuestionnaireUserDTO;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questionnaires")
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
        if (questionnaireDTO.getTitle() == null || questionnaireDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required.");
        }

        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setTitle(questionnaireDTO.getTitle().trim());
        questionnaire.setQuestions(List.of()); // Ensure non-null

        Questionnaire saved = questionnaireService.createQuestionnaire(questionnaire);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Questionnaire> importQuestionnaire(@RequestBody @Valid QuestionnaireDTO dto) {
        return ResponseEntity.ok(questionnaireService.importQuestionnaireDto(dto));
    }

    @GetMapping
    public List<QuestionnaireUserDTO> getAllQuestionnaires() {
        return questionnaireService.getAllQuestionnaires().stream()
                .map(QuestionnaireUserDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<QuestionnaireDTO> getAllQuestionnairesForAdmin() {
        return questionnaireService.getAllQuestionnaires().stream()
                .map(QuestionnaireDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Questionnaire> getQuestionnaireById(@PathVariable Long id) {
        return questionnaireService.getQuestionnaireById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new QuestionnaireNotFoundException("Questionnaire not found with ID: " + id));
    }

    @GetMapping("/{questionnaireId}/category/{categoryName}")
    public List<QuestionUserDTO> getQuestionsByQuestionnaireAndCategory(
            @PathVariable Long questionnaireId,
            @PathVariable String categoryName
    ) {
        Questionnaire q = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new QuestionnaireNotFoundException("Questionnaire not found with ID: " + questionnaireId));

        return q.getQuestions().stream()
                .filter(question -> question.getCategory() != null &&
                        question.getCategory().getName().equalsIgnoreCase(categoryName))
                .map(QuestionUserDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{questionnaireId}/category/{categoryName}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Question> getQuestionsByQuestionnaireAndCategoryForAdmin(
            @PathVariable Long questionnaireId,
            @PathVariable String categoryName
    ) {
        Questionnaire q = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new QuestionnaireNotFoundException("Questionnaire not found with ID: " + questionnaireId));

        return q.getQuestions().stream()
                .filter(question -> question.getCategory() != null &&
                        question.getCategory().getName().equalsIgnoreCase(categoryName))
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/add-question")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Question> addQuestionToQuestionnaire(
            @PathVariable Long id,
            @RequestBody @Valid QuestionDTO dto
    ) {
        if (dto.getQuestionText() == null || dto.getQuestionText().trim().isEmpty()) {
            throw new IllegalArgumentException("Question text must not be empty.");
        }
        if (dto.getCategoryName() == null || dto.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name must not be empty.");
        }

        Question added = questionnaireService.addQuestionDtoToQuestionnaire(id, dto);
        return ResponseEntity.ok(added);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuestionnaire(@PathVariable Long id) {
        questionnaireService.deleteQuestionnaire(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportQuestionnaire(@PathVariable Long id) throws JsonProcessingException {
        Questionnaire questionnaire = questionnaireService.getQuestionnaireById(id)
                .orElseThrow(() -> new NotFoundException("Questionnaire not found for ID: " + id));

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(questionnaire);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=\"questionnaire_" + id + ".json\"");
        headers.add("Content-Type", "application/json; charset=UTF-8");

        return new ResponseEntity<>(json.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Questionnaire updateQuestionnaire(@PathVariable Long id, @RequestBody @Valid Questionnaire updatedQuestionnaire) {
        if (updatedQuestionnaire.getTitle() == null || updatedQuestionnaire.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Updated title must not be empty.");
        }

        return questionnaireService.updateQuestionnaire(id, updatedQuestionnaire);
    }

    @GetMapping("/{id}/questions")
    public List<QuestionUserDTO> getAllQuestionsForQuestionnaire(@PathVariable Long id) {
        return questionnaireService.getAllQuestionsForQuestionnaire(id)
                .stream()
                .map(QuestionUserDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/questions/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Question> getAllQuestionsForQuestionnaireForAdmin(@PathVariable Long id) {
        return questionnaireService.getAllQuestionsForQuestionnaire(id);
    }

    @GetMapping("/search")
    public List<QuestionnaireUserDTO> searchQuestionnaires(@RequestParam(required = false) String title) {
        return questionnaireService.searchQuestionnaires(title)
                .stream()
                .map(QuestionnaireUserDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/search/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<QuestionnaireDTO> searchQuestionnairesForAdmin(@RequestParam(required = false) String title) {
        return questionnaireService.searchQuestionnaires(title)
                .stream()
                .map(QuestionnaireDTO::new)
                .collect(Collectors.toList());
    }
}
