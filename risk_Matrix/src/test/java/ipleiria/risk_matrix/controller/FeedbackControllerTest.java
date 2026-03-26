package ipleiria.risk_matrix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipleiria.risk_matrix.dto.FeedbackRequestDTO;
import ipleiria.risk_matrix.models.feedback.Feedback;
import ipleiria.risk_matrix.models.feedback.FeedbackType;
import ipleiria.risk_matrix.service.FeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeedbackController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcTestConfig.class)
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FeedbackService feedbackService;

    private Feedback feedback;
    private FeedbackRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        feedback = Feedback.builder()
                .email("user@example.com")
                .userFeedback("Great tool!")
                .feedbackType(FeedbackType.SUGGESTION)
                .build();

        requestDTO = new FeedbackRequestDTO();
        requestDTO.setEmail("user@example.com");
        requestDTO.setUserFeedback("Great tool!");
        requestDTO.setFeedbackType(FeedbackType.SUGGESTION);
    }

    @Test
    void createFeedback_validInput_returns200() throws Exception {
        when(feedbackService.saveFeedback(any(FeedbackRequestDTO.class))).thenReturn(feedback);

        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("user@example.com")))
                .andExpect(jsonPath("$.feedbackType", is("SUGGESTION")));
    }

    @Test
    void getAllFeedback_returnsPaginatedResults() throws Exception {
        Page<Feedback> page = new PageImpl<>(List.of(feedback));
        when(feedbackService.getAllFeedback(anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/feedback")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email", is("user@example.com")));
    }

    @Test
    void filterFeedback_withParams_returns200() throws Exception {
        when(feedbackService.filterFeedback(any(), any(), any(), any()))
                .thenReturn(List.of(feedback));

        mockMvc.perform(get("/api/feedback/filter")
                        .param("email", "user@example.com")
                        .param("type", "SUGGESTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void filterFeedback_invalidType_returns400() throws Exception {
        mockMvc.perform(get("/api/feedback/filter")
                        .param("type", "INVALID_TYPE"))
                .andExpect(status().isBadRequest());
    }
}
