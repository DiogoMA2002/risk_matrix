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

    public List<Feedback> getFeedbackByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must be provided.");
        }
        return feedbackRepository.findByEmail(email.trim());
    }

    public List<Feedback> getFeedbackByType(FeedbackType type) {
        if (type == null) {
            throw new IllegalArgumentException("Feedback type must be provided.");
        }
        return feedbackRepository.findByFeedbackType(type);
    }

    public List<Feedback> getFeedbackByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        validateDateRange(startDate, endDate);
        return feedbackRepository.findByCreatedAtBetween(startDate, endDate);
    }

    public List<Feedback> getFeedbackByEmailAndType(String email, FeedbackType type) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must be provided.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Feedback type must be provided.");
        }
        return feedbackRepository.findByEmailAndFeedbackType(email.trim(), type);
    }

    public List<Feedback> getFeedbackByEmailAndDateRange(String email, LocalDateTime startDate, LocalDateTime endDate) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must be provided.");
        }
        validateDateRange(startDate, endDate);
        return feedbackRepository.findByEmailAndCreatedAtBetween(email.trim(), startDate, endDate);
    }

    public List<Feedback> getFeedbackByTypeAndDateRange(FeedbackType type, LocalDateTime startDate, LocalDateTime endDate) {
        if (type == null) {
            throw new IllegalArgumentException("Feedback type must be provided.");
        }
        validateDateRange(startDate, endDate);
        return feedbackRepository.findByFeedbackTypeAndCreatedAtBetween(type, startDate, endDate);
    }

    public List<Feedback> getFeedbackByEmailAndTypeAndDateRange(String email, FeedbackType type, LocalDateTime startDate, LocalDateTime endDate) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must be provided.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Feedback type must be provided.");
        }
        validateDateRange(startDate, endDate);
        return feedbackRepository.findByEmailAndFeedbackTypeAndCreatedAtBetween(email.trim(), type, startDate, endDate);
    }
    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates must be provided.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
    }

}
