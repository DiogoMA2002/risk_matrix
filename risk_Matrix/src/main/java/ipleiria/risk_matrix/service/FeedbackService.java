package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.exceptions.exception.FeedbackTooLongException;
import ipleiria.risk_matrix.models.feedback.Feedback;
import ipleiria.risk_matrix.models.feedback.FeedbackType;
import ipleiria.risk_matrix.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private static final int MAX_WORDS = 250;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }


    public Feedback saveFeedback(Feedback feedback) {
        int wordCount = feedback.getUserFeedback().trim().split("\\s+").length;
        if (wordCount > MAX_WORDS) {
            throw new FeedbackTooLongException(
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
