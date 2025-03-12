package ipleiria.risk_matrix.controller;

import ipleiria.risk_matrix.models.feedback.Feedback;
import ipleiria.risk_matrix.models.feedback.FeedbackType;
import ipleiria.risk_matrix.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Feedback> submitFeedback(@RequestBody Feedback feedback) {
        Feedback savedFeedback = feedbackService.saveFeedback(feedback);
        return ResponseEntity.ok(savedFeedback);
    }

    @GetMapping
    public ResponseEntity<List<Feedback>> getFeedback(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) FeedbackType type) {

        List<Feedback> feedbackList;
        if (email != null && !email.trim().isEmpty() && type != null) {
            feedbackList = feedbackService.getFeedbackByEmailAndType(email, type);
        } else if (email != null && !email.trim().isEmpty()) {
            feedbackList = feedbackService.getFeedbackByEmail(email);
        } else if (type != null) {
            feedbackList = feedbackService.getFeedbackByType(type);
        } else {
            feedbackList = feedbackService.getAllFeedback();
        }
        return ResponseEntity.ok(feedbackList);
    }
}
