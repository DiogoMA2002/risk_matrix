package ipleiria.risk_matrix.controller;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.dto.UserAnswersDTO;
import ipleiria.risk_matrix.service.AnswerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    // Obter todas as respostas
    @GetMapping("/all")
    public List<AnswerDTO> getAllAnswers() {
        return answerService.getAllAnswers();
    }

    @PostMapping("/submit")
    public AnswerDTO submitAnswer(@Valid @RequestBody AnswerDTO answerDTO) {
        return answerService.submitAnswer(answerDTO);
    }

    @GetMapping("/by-question/{questionId}")
    public List<AnswerDTO> getAnswersByQuestion(@PathVariable Long questionId) {
        return answerService.getAnswersByQuestion(questionId);
    }

    // Get answers by user email
    @GetMapping("/by-email/{email}")
    public List<AnswerDTO> getAnswersByEmail(@PathVariable String email) {
        return answerService.getAnswersByEmail(email);
    }

    // Get user submissions with severity (grouped by submissionId) for a specific email
    @GetMapping("/by-email-with-severity/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserAnswersDTO> getUserSubmissionsWithSeverities(@PathVariable String email) {
        return answerService.getUserSubmissionsWithSeverities(email);
    }

    // Get all submissions (grouped by submissionId) with severity and email information
    @GetMapping("/get-all-submissions")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserAnswersDTO> getAllSubmissionsWithSeverityAndEmail() {
        return answerService.getAllSubmissionsWithSeverityAndEmail();
    }

    @PostMapping("/submit-multiple")
    public List<AnswerDTO> submitMultipleAnswers(@Valid @RequestBody List<@Valid AnswerDTO> answers) {
        return answerService.submitMultipleAnswers(answers);
    }

    @GetMapping("/by-date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AnswerDTO> getAnswersByDateRange(@RequestParam String startDate, @RequestParam String endDate) {
        return answerService.getAnswersByDateRange(startDate, endDate);
    }
}
