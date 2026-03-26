package ipleiria.risk_matrix.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.dto.QuestionnaireDTO;
import ipleiria.risk_matrix.dto.QuestionUserDTO;
import ipleiria.risk_matrix.dto.QuestionnaireUserDTO;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.exceptions.exception.QuestionnaireNotFoundException;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
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
@Tag(name = "Questionnaires", description = "CRUD and import/export operations for questionnaires")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;
    private final ObjectMapper objectMapper;

    public QuestionnaireController(QuestionnaireService questionnaireService, ObjectMapper objectMapper) {
        this.questionnaireService = questionnaireService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a questionnaire", description = "Creates a new empty questionnaire. Requires ADMIN role.")
    @ApiResponse(responseCode = "201", description = "Questionnaire created")
    public ResponseEntity<QuestionnaireDTO> createQuestionnaire(@RequestBody @Valid QuestionnaireDTO questionnaireDTO) {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setTitle(questionnaireDTO.getTitle().trim());
        questionnaire.setQuestions(List.of());

        Questionnaire saved = questionnaireService.createQuestionnaire(questionnaire);
        return ResponseEntity.status(HttpStatus.CREATED).body(new QuestionnaireDTO(saved));
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Import a questionnaire", description = "Imports a full questionnaire with questions and options from JSON. Reuses existing questions to avoid duplicates. Requires ADMIN role.")
    public ResponseEntity<QuestionnaireDTO> importQuestionnaire(@RequestBody @Valid QuestionnaireDTO dto) {
        return ResponseEntity.ok(new QuestionnaireDTO(questionnaireService.importQuestionnaireDto(dto)));
    }

    @GetMapping
    @Operation(summary = "List all questionnaires (public view)", description = "Returns all questionnaires with user-facing fields only")
    public List<QuestionnaireUserDTO> getAllQuestionnaires() {
        return questionnaireService.getAllQuestionnaires().stream()
                .map(QuestionnaireUserDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all questionnaires (admin view)", description = "Returns all questionnaires with full admin-level detail. Requires ADMIN role.")
    public List<QuestionnaireDTO> getAllQuestionnairesForAdmin() {
        return questionnaireService.getAllQuestionnaires().stream()
                .map(QuestionnaireDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get questionnaire by ID", description = "Returns a single questionnaire by its ID (public view)")
    @ApiResponse(responseCode = "200", description = "Questionnaire found")
    @ApiResponse(responseCode = "404", description = "Questionnaire not found")
    public ResponseEntity<QuestionnaireUserDTO> getQuestionnaireById(@Parameter(description = "Questionnaire ID") @PathVariable Long id) {
        return questionnaireService.getQuestionnaireById(id)
                .map(q -> ResponseEntity.ok(new QuestionnaireUserDTO(q)))
                .orElseThrow(() -> new QuestionnaireNotFoundException("Questionnaire not found with ID: " + id));
    }

    @GetMapping("/{questionnaireId}/category/{categoryName}")
    @Operation(summary = "Get questions by questionnaire and category", description = "Returns questions filtered by questionnaire ID and category name (public view)")
    public List<QuestionUserDTO> getQuestionsByQuestionnaireAndCategory(
            @Parameter(description = "Questionnaire ID") @PathVariable Long questionnaireId,
            @Parameter(description = "Category name") @PathVariable String categoryName) {
        return questionnaireService.getQuestionsByCategory(questionnaireId, categoryName).stream()
                .map(QuestionUserDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{questionnaireId}/category/{categoryName}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get questions by questionnaire and category (admin)", description = "Returns questions with full admin detail filtered by questionnaire and category. Requires ADMIN role.")
    public List<QuestionDTO> getQuestionsByQuestionnaireAndCategoryForAdmin(
            @Parameter(description = "Questionnaire ID") @PathVariable Long questionnaireId,
            @Parameter(description = "Category name") @PathVariable String categoryName) {
        return questionnaireService.getQuestionsByCategory(questionnaireId, categoryName).stream()
                .map(QuestionDTO::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/add-question")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a question to a questionnaire", description = "Adds a new question to the specified questionnaire. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Question added")
    @ApiResponse(responseCode = "404", description = "Questionnaire not found")
    public ResponseEntity<QuestionDTO> addQuestionToQuestionnaire(
            @Parameter(description = "Questionnaire ID") @PathVariable Long id,
            @RequestBody @Valid QuestionDTO dto) {
        return ResponseEntity.ok(new QuestionDTO(questionnaireService.addQuestionDtoToQuestionnaire(id, dto)));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a questionnaire", description = "Deletes a questionnaire and orphan questions. Requires ADMIN role.")
    @ApiResponse(responseCode = "204", description = "Questionnaire deleted")
    @ApiResponse(responseCode = "404", description = "Questionnaire not found")
    public ResponseEntity<Void> deleteQuestionnaire(@Parameter(description = "Questionnaire ID") @PathVariable Long id) {
        questionnaireService.deleteQuestionnaire(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export questionnaire as JSON", description = "Downloads the questionnaire as a JSON file attachment. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "JSON file downloaded")
    @ApiResponse(responseCode = "404", description = "Questionnaire not found")
    public ResponseEntity<byte[]> exportQuestionnaire(@Parameter(description = "Questionnaire ID") @PathVariable Long id) throws JsonProcessingException {
        Questionnaire questionnaire = questionnaireService.getQuestionnaireById(id)
                .orElseThrow(() -> new NotFoundException("Questionnaire not found for ID: " + id));

        QuestionnaireDTO dto = new QuestionnaireDTO(questionnaire);
        String json = objectMapper.writeValueAsString(dto);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=\"questionnaire_" + id + ".json\"");
        headers.add("Content-Type", "application/json; charset=UTF-8");

        return new ResponseEntity<>(json.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a questionnaire", description = "Updates the title of an existing questionnaire. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Questionnaire updated")
    @ApiResponse(responseCode = "404", description = "Questionnaire not found")
    public QuestionnaireDTO updateQuestionnaire(@Parameter(description = "Questionnaire ID") @PathVariable Long id, @RequestBody @Valid QuestionnaireDTO dto) {
        Questionnaire toUpdate = new Questionnaire();
        toUpdate.setTitle(dto.getTitle().trim());
        return new QuestionnaireDTO(questionnaireService.updateQuestionnaire(id, toUpdate));
    }

    @GetMapping("/{id}/questions")
    @Operation(summary = "Get all questions for a questionnaire (public view)")
    public List<QuestionUserDTO> getAllQuestionsForQuestionnaire(@Parameter(description = "Questionnaire ID") @PathVariable Long id) {
        return questionnaireService.getAllQuestionsForQuestionnaire(id).stream()
                .map(QuestionUserDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/questions/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all questions for a questionnaire (admin view)", description = "Requires ADMIN role.")
    public List<QuestionDTO> getAllQuestionsForQuestionnaireForAdmin(@Parameter(description = "Questionnaire ID") @PathVariable Long id) {
        return questionnaireService.getAllQuestionsForQuestionnaire(id).stream()
                .map(QuestionDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    @Operation(summary = "Search questionnaires by title (public view)")
    public List<QuestionnaireUserDTO> searchQuestionnaires(@Parameter(description = "Title filter (partial match)") @RequestParam(required = false) String title) {
        return questionnaireService.searchQuestionnaires(title).stream()
                .map(QuestionnaireUserDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/search/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search questionnaires by title (admin view)", description = "Requires ADMIN role.")
    public List<QuestionnaireDTO> searchQuestionnairesForAdmin(@Parameter(description = "Title filter (partial match)") @RequestParam(required = false) String title) {
        return questionnaireService.searchQuestionnaires(title).stream()
                .map(QuestionnaireDTO::new)
                .collect(Collectors.toList());
    }
}
