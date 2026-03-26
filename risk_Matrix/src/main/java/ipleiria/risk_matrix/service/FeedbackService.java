package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.FeedbackRequestDTO;
import ipleiria.risk_matrix.models.feedback.Feedback;
import ipleiria.risk_matrix.models.feedback.FeedbackType;
import ipleiria.risk_matrix.repository.FeedbackRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackService {

    private static final int MAX_PAGE_SIZE = 200;

    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public Feedback saveFeedback(FeedbackRequestDTO dto) {
        Feedback feedback = Feedback.builder()
                .email(dto.getEmail().trim())
                .userFeedback(dto.getUserFeedback().trim())
                .feedbackType(dto.getFeedbackType())
                .build();
        return feedbackRepository.save(feedback);
    }

    public Page<Feedback> getAllFeedback(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return feedbackRepository.findAll(pageable);
    }

    public List<Feedback> filterFeedback(String email, FeedbackType type,
                                         LocalDateTime startDate, LocalDateTime endDate) {
        return feedbackRepository.filterFeedback(email, type, startDate, endDate);
    }
}
