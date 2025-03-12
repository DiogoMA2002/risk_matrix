package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.models.feedback.Feedback;
import ipleiria.risk_matrix.models.feedback.FeedbackType;
import ipleiria.risk_matrix.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import jakarta.validation.ValidationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private static final int MAX_WORDS = 250;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public Feedback saveFeedback(Feedback feedback) {
        int wordCount = feedback.getUserFeedback().trim().split("\\s+").length;
        if (wordCount > MAX_WORDS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, // or HttpStatus.UNPROCESSABLE_ENTITY
                    "Feedback cannot exceed " + MAX_WORDS + " words."
            );
        }
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    public List<Feedback> getFeedbackByEmail(String email) {
        return feedbackRepository.findByEmail(email);
    }

    public List<Feedback> getFeedbackByType(FeedbackType type) {
        return feedbackRepository.findByFeedbackType(type);
    }

    public List<Feedback> getFeedbackByEmailAndType(String email, FeedbackType type) {
        return feedbackRepository.findByEmailAndFeedbackType(email, type);
    }
}
