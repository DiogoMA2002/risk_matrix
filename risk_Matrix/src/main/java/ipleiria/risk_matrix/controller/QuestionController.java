package ipleiria.risk_matrix.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Questions", description = "CRUD operations for risk assessment questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a question", description = "Creates a new question and associates it with questionnaires if IDs are provided. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Question created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid question data")
    public QuestionDTO create(@RequestBody @Valid QuestionDTO dto) {
        return questionService.createQuestion(dto);
    }

    @GetMapping
    @Operation(summary = "Get all questions", description = "Returns all questions with their categories and options")
    public List<QuestionDTO> getAll() {
        return questionService.getAllQuestions().stream()
                .map(QuestionDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/category/{categoryName}")
    @Operation(summary = "Get questions by category", description = "Returns all questions belonging to the specified category")
    public List<QuestionDTO> getByCategory(
            @Parameter(description = "Category name") @PathVariable String categoryName) {
        return questionService.getQuestionsByCategory(categoryName).stream()
                .map(QuestionDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get question by ID", description = "Returns a single question by its ID")
    @ApiResponse(responseCode = "200", description = "Question found")
    @ApiResponse(responseCode = "404", description = "Question not found")
    public QuestionDTO getById(@Parameter(description = "Question ID") @PathVariable Long id) {
        return questionService.getQuestionDtoById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a question", description = "Updates an existing question. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Question updated successfully")
    @ApiResponse(responseCode = "404", description = "Question not found")
    @ApiResponse(responseCode = "409", description = "Duplicate question text in the same category")
    public QuestionDTO update(
            @Parameter(description = "Question ID") @PathVariable Long id,
            @RequestBody @Valid QuestionDTO dto) {
        return questionService.updateQuestion(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a question", description = "Deletes a question and removes it from all associated questionnaires. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Question deleted successfully")
    @ApiResponse(responseCode = "404", description = "Question not found")
    public void delete(@Parameter(description = "Question ID") @PathVariable Long id) {
        questionService.deleteQuestion(id);
    }
}
