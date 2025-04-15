package ipleiria.risk_matrix.controller;

import ipleiria.risk_matrix.models.feedback.Feedback;
import ipleiria.risk_matrix.models.feedback.FeedbackType;
import ipleiria.risk_matrix.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
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

    // Endpoint for users to submit feedback (either a suggestion or help request)
    @PostMapping
    public ResponseEntity<Feedback> createFeedback(@RequestBody Feedback feedback) {
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
        try {
            FeedbackType feedbackType = FeedbackType.valueOf(type.toUpperCase());
            return ResponseEntity.ok(feedbackService.getFeedbackByType(feedbackType));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/email/{email}/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> getFeedbackByEmailAndType(
            @PathVariable String email,
            @PathVariable String type) {
        try {
            FeedbackType feedbackType = FeedbackType.valueOf(type.toUpperCase());
            return ResponseEntity.ok(feedbackService.getFeedbackByEmailAndType(email, feedbackType));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> getFeedbackByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(feedbackService.getFeedbackByDateRange(startDate, endDate));
    }

    @GetMapping("/email/{email}/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> getFeedbackByEmailAndDateRange(
            @PathVariable String email,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(feedbackService.getFeedbackByEmailAndDateRange(email, startDate, endDate));
    }

    @GetMapping("/type/{type}/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> getFeedbackByTypeAndDateRange(
            @PathVariable String type,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        try {
            FeedbackType feedbackType = FeedbackType.valueOf(type.toUpperCase());
            return ResponseEntity.ok(feedbackService.getFeedbackByTypeAndDateRange(feedbackType, startDate, endDate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/email/{email}/type/{type}/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> getFeedbackByEmailAndTypeAndDateRange(
            @PathVariable String email,
            @PathVariable String type,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        try {
            FeedbackType feedbackType = FeedbackType.valueOf(type.toUpperCase());
            return ResponseEntity.ok(feedbackService.getFeedbackByEmailAndTypeAndDateRange(email, feedbackType, startDate, endDate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
