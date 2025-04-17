package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.exceptions.exception.FeedbackTooLongException;
import ipleiria.risk_matrix.models.feedback.Feedback;
import ipleiria.risk_matrix.models.feedback.FeedbackType;
import ipleiria.risk_matrix.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private static final int MAX_WORDS = 250;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }


    public Feedback saveFeedback(Feedback feedback) {
        if (feedback == null) {
            throw new IllegalArgumentException("Feedback object must not be null.");
        }

        String feedbackText = feedback.getUserFeedback();
        if (feedbackText == null || feedbackText.trim().isEmpty()) {
            throw new IllegalArgumentException("Feedback content cannot be empty.");
        }

        int wordCount = feedbackText.trim().split("\\s+").length;
        if (wordCount > MAX_WORDS) {
            throw new FeedbackTooLongException("Feedback cannot exceed " + MAX_WORDS + " words.");
        }

        return feedbackRepository.save(feedback);
    }


    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }


    public List<Feedback> filterFeedback(String email, FeedbackType type, LocalDateTime startDate, LocalDateTime endDate) {
        return feedbackRepository.findAll().stream()
                .filter(fb -> email == null || fb.getEmail().equalsIgnoreCase(email))
                .filter(fb -> type == null || fb.getFeedbackType() == type)
                .filter(fb -> startDate == null || !fb.getCreatedAt().isBefore(startDate))
                .filter(fb -> endDate == null || !fb.getCreatedAt().isAfter(endDate))
                .toList();
    }

}
