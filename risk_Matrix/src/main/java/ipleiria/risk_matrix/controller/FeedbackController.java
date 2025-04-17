package ipleiria.risk_matrix.controller;

import ipleiria.risk_matrix.exceptions.exception.InvalidFeedbackTypeException;
import ipleiria.risk_matrix.models.feedback.Feedback;
import ipleiria.risk_matrix.models.feedback.FeedbackType;
import ipleiria.risk_matrix.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Autowired
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<Feedback> createFeedback(@Valid @RequestBody Feedback feedback) {
        return ResponseEntity.ok(feedbackService.saveFeedback(feedback));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        return ResponseEntity.ok(feedbackService.getAllFeedback());
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> filterFeedback(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        FeedbackType feedbackType = null;
        if (type != null && !type.isBlank()) {
            feedbackType = parseFeedbackType(type);
        }

        List<Feedback> result = feedbackService.filterFeedback(email, feedbackType, startDate, endDate);
        return ResponseEntity.ok(result);
    }


    private FeedbackType parseFeedbackType(String type) {
        try {
            return FeedbackType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidFeedbackTypeException("Invalid feedback type: " + type);
        }
    }
}
