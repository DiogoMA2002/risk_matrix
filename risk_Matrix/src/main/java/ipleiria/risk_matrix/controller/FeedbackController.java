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

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> getFeedbackByEmail(@PathVariable String email) {
        return ResponseEntity.ok(feedbackService.getFeedbackByEmail(email));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> getFeedbackByType(@PathVariable String type) {
        return ResponseEntity.ok(feedbackService.getFeedbackByType(parseFeedbackType(type)));
    }

    @GetMapping("/email/{email}/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> getFeedbackByEmailAndType(
            @PathVariable String email,
            @PathVariable String type) {
        return ResponseEntity.ok(feedbackService.getFeedbackByEmailAndType(email, parseFeedbackType(type)));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> getFeedbackByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(feedbackService.getFeedbackByDateRange(startDate, endDate));
    }

    @GetMapping("/email/{email}/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> getFeedbackByEmailAndDateRange(
            @PathVariable String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(feedbackService.getFeedbackByEmailAndDateRange(email, startDate, endDate));
    }

    @GetMapping("/type/{type}/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> getFeedbackByTypeAndDateRange(
            @PathVariable String type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(feedbackService.getFeedbackByTypeAndDateRange(parseFeedbackType(type), startDate, endDate));
    }

    @GetMapping("/email/{email}/type/{type}/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> getFeedbackByEmailAndTypeAndDateRange(
            @PathVariable String email,
            @PathVariable String type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(
                feedbackService.getFeedbackByEmailAndTypeAndDateRange(email, parseFeedbackType(type), startDate, endDate)
        );
    }

    private FeedbackType parseFeedbackType(String type) {
        try {
            return FeedbackType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidFeedbackTypeException("Invalid feedback type: " + type);
        }
    }
}
