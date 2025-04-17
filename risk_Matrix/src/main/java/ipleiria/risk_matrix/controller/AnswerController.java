package ipleiria.risk_matrix.controller;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.dto.UserAnswersDTO;
import ipleiria.risk_matrix.service.AnswerService;
import ipleiria.risk_matrix.service.DocumentsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/answers")
public class AnswerController {

    private final AnswerService answerService;
    private final DocumentsService documentsService;

    public AnswerController(AnswerService answerService, DocumentsService documentsService) {
        this.answerService = answerService;
        this.documentsService = documentsService;
    }

    @PostMapping("/submit-multiple")
    public List<AnswerDTO> submitMultipleAnswers(@Valid @RequestBody List<@Valid AnswerDTO> answers) {
        return answerService.submitMultipleAnswers(answers);
    }


    @GetMapping("/by-email-with-severity/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserAnswersDTO> getUserSubmissionsWithSeverities(@PathVariable String email) {
        return answerService.getUserSubmissionsWithSeverities(email);
    }

    @GetMapping("/get-all-submissions")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserAnswersDTO> getAllSubmissionsWithSeverityAndEmail() {
        return answerService.getAllSubmissionsWithSeverityAndEmail();
    }

    @GetMapping("/by-date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserAnswersDTO> getAnswersByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        if (startDate == null || endDate == null || startDate.isBlank() || endDate.isBlank()) {
            throw new IllegalArgumentException("Start and end dates must be provided.");
        }
        return answerService.getAnswersByDateRange(startDate, endDate);
    }

    @GetMapping("/export-submission/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportSubmission(@PathVariable String id) throws IOException {
        byte[] docBytes = documentsService.generateEnhancedDocx(id);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report_" + id + ".docx\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(docBytes);
    }
}
