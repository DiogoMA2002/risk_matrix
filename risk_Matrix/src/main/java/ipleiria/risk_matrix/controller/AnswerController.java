package ipleiria.risk_matrix.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.dto.UserAnswersDTO;
import ipleiria.risk_matrix.service.AnswerService;
import ipleiria.risk_matrix.service.DocumentsService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/answers")
@Tag(name = "Answers", description = "Submit and query risk assessment answers")
public class AnswerController {

    private final AnswerService answerService;
    private final DocumentsService documentsService;

    public AnswerController(AnswerService answerService, DocumentsService documentsService) {
        this.answerService = answerService;
        this.documentsService = documentsService;
    }

    @PostMapping("/submit-multiple")
    @Operation(summary = "Submit multiple answers", description = "Submits a batch of answers for a single submission session. A shared submission ID is generated automatically.")
    @ApiResponse(responseCode = "200", description = "Answers submitted")
    @ApiResponse(responseCode = "400", description = "Invalid answer data")
    public List<AnswerDTO> submitMultipleAnswers(@Valid @RequestBody List<@Valid AnswerDTO> answers) {
        return answerService.submitMultipleAnswers(answers);
    }

    @GetMapping("/by-email-with-severity/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get submissions by email with severity", description = "Returns all submissions for a user email with computed severity per category. Requires ADMIN role.")
    public List<UserAnswersDTO> getUserSubmissionsWithSeverities(
            @Parameter(description = "User email") @PathVariable String email) {
        return answerService.getUserSubmissionsWithSeverities(email);
    }

    @GetMapping("/get-all-submissions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all submissions (paginated)", description = "Returns a page of submissions with severity information. Includes totalElements and totalPages for pagination. Requires ADMIN role.")
    public Page<UserAnswersDTO> getAllSubmissionsWithSeverityAndEmail(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 500)") @RequestParam(defaultValue = "20") int size) {
        return answerService.getAllSubmissionsWithSeverityAndEmail(page, size);
    }

    @GetMapping("/by-date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get submissions by date range (paginated)", description = "Returns a page of submissions within the specified date range. Includes totalElements and totalPages for pagination. Requires ADMIN role.")
    public Page<UserAnswersDTO> getAnswersByDateRange(
            @Parameter(description = "Start date (ISO date)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO date)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 500)") @RequestParam(defaultValue = "20") int size) {
        return answerService.getAnswersByDateRange(startDate, endDate, page, size);
    }

    @GetMapping("/export-submission/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export submission as DOCX", description = "Generates and downloads an enhanced DOCX report for a submission. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "DOCX file downloaded")
    @ApiResponse(responseCode = "404", description = "Submission not found")
    public ResponseEntity<byte[]> exportSubmission(
            @Parameter(description = "Submission ID") @PathVariable String id) throws IOException {
        byte[] docBytes = documentsService.generateEnhancedDocx(id);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report_" + id + ".docx\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(docBytes);
    }

    @DeleteMapping("/submission/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a submission", description = "Permanently deletes all answers for the given submission ID. Requires ADMIN role.")
    @ApiResponse(responseCode = "204", description = "Submission deleted")
    @ApiResponse(responseCode = "404", description = "Submission not found")
    public ResponseEntity<Void> deleteSubmission(
            @Parameter(description = "Submission ID") @PathVariable String id) {
        answerService.deleteSubmission(id);
        return ResponseEntity.noContent().build();
    }
}
