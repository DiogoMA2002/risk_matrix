package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.FeedbackRequestDTO;
import ipleiria.risk_matrix.models.feedback.Feedback;
import ipleiria.risk_matrix.models.feedback.FeedbackType;
import ipleiria.risk_matrix.repository.FeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private FeedbackRequestDTO requestDTO;
    private Feedback feedback;

    @BeforeEach
    void setUp() {
        requestDTO = new FeedbackRequestDTO();
        requestDTO.setEmail("user@example.com");
        requestDTO.setUserFeedback("Great tool!");
        requestDTO.setFeedbackType(FeedbackType.SUGGESTION);

        feedback = Feedback.builder()
                .email("user@example.com")
                .userFeedback("Great tool!")
                .feedbackType(FeedbackType.SUGGESTION)
                .build();
    }

    @Test
    void saveFeedback_validInput_savesAndReturns() {
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

        Feedback result = feedbackService.saveFeedback(requestDTO);

        assertThat(result.getEmail()).isEqualTo("user@example.com");
        assertThat(result.getUserFeedback()).isEqualTo("Great tool!");
        assertThat(result.getFeedbackType()).isEqualTo(FeedbackType.SUGGESTION);

        ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackRepository).save(captor.capture());
        Feedback saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        assertThat(saved.getUserFeedback()).isEqualTo("Great tool!");
    }

    @Test
    void saveFeedback_trimsWhitespace() {
        requestDTO.setEmail("  user@example.com  ");
        requestDTO.setUserFeedback("  Some feedback  ");
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(inv -> inv.getArgument(0));

        Feedback result = feedbackService.saveFeedback(requestDTO);

        assertThat(result.getEmail()).isEqualTo("user@example.com");
        assertThat(result.getUserFeedback()).isEqualTo("Some feedback");
    }

    @Test
    void getAllFeedback_returnsPaginatedResults() {
        Page<Feedback> page = new PageImpl<>(List.of(feedback));
        when(feedbackRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Feedback> result = feedbackService.getAllFeedback(0, 50);

        assertThat(result.getContent()).hasSize(1);
        verify(feedbackRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllFeedback_capsPageSizeAt200() {
        Page<Feedback> page = new PageImpl<>(List.of());
        when(feedbackRepository.findAll(any(Pageable.class))).thenReturn(page);

        feedbackService.getAllFeedback(0, 999);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(feedbackRepository).findAll(captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(200);
    }

    @Test
    void filterFeedback_allParams_delegatesToRepository() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 31, 23, 59);

        when(feedbackRepository.filterFeedback("user@example.com", FeedbackType.SUGGESTION, start, end))
                .thenReturn(List.of(feedback));

        List<Feedback> result = feedbackService.filterFeedback("user@example.com", FeedbackType.SUGGESTION, start, end);

        assertThat(result).hasSize(1);
        verify(feedbackRepository).filterFeedback("user@example.com", FeedbackType.SUGGESTION, start, end);
    }

    @Test
    void filterFeedback_nullParams_delegatesToRepository() {
        when(feedbackRepository.filterFeedback(null, null, null, null))
                .thenReturn(List.of(feedback));

        List<Feedback> result = feedbackService.filterFeedback(null, null, null, null);

        assertThat(result).hasSize(1);
    }
}
