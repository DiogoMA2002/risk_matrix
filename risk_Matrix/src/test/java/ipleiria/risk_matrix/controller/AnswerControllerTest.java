package ipleiria.risk_matrix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.dto.UserAnswersDTO;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.service.AnswerService;
import ipleiria.risk_matrix.service.DocumentsService;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnswerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcTestConfig.class)
class AnswerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AnswerService answerService;

    @MockitoBean
    private DocumentsService documentsService;

    private AnswerDTO answerDTO;

    @BeforeEach
    void setUp() {
        answerDTO = new AnswerDTO();
        answerDTO.setQuestionId(10L);
        answerDTO.setUserResponse("Yes");
        answerDTO.setEmail("user@example.com");
        answerDTO.setQuestionType(OptionLevelType.IMPACT);
        answerDTO.setChosenLevel(OptionLevel.LOW);
        answerDTO.setSubmissionId("sub-1");
        answerDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void submitMultipleAnswers_returns200() throws Exception {
        when(answerService.submitMultipleAnswers(any())).thenReturn(List.of(answerDTO));

        mockMvc.perform(post("/api/answers/submit-multiple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(answerDTO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getUserSubmissionsWithSeverities_returns200() throws Exception {
        UserAnswersDTO dto = new UserAnswersDTO();
        dto.setSubmissionId("sub-1");
        dto.setEmail("user@example.com");
        dto.setAnswers(List.of(answerDTO));
        dto.setSeveritiesByCategory(Map.of());

        when(answerService.getUserSubmissionsWithSeverities("user@example.com")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/answers/by-email-with-severity/user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void exportSubmission_returnsAttachment() throws Exception {
        when(documentsService.generateEnhancedDocx("sub-1")).thenReturn("doc".getBytes());

        mockMvc.perform(get("/api/answers/export-submission/sub-1"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("report_sub-1.docx")))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    void getAnswersByDateRange_returnsPaginatedResponse() throws Exception {
        UserAnswersDTO dto = new UserAnswersDTO();
        dto.setSubmissionId("sub-1");
        dto.setEmail("user@example.com");
        dto.setAnswers(List.of(answerDTO));
        dto.setSeveritiesByCategory(Map.of());

        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 15);
        when(answerService.getAnswersByDateRange(any(), any(), eq(0), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/answers/by-date-range")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].submissionId").value("sub-1"));
    }

    @Test
    void getAllSubmissions_returnsPaginatedResponse() throws Exception {
        UserAnswersDTO dto = new UserAnswersDTO();
        dto.setSubmissionId("sub-1");
        dto.setEmail("user@example.com");
        dto.setAnswers(List.of(answerDTO));
        dto.setSeveritiesByCategory(Map.of());

        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 42);
        when(answerService.getAllSubmissionsWithSeverityAndEmail(0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/answers/get-all-submissions").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(42))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.content[0].submissionId").value("sub-1"));
    }

    @Test
    void deleteSubmission_returns204() throws Exception {
        doNothing().when(answerService).deleteSubmission("sub-1");

        mockMvc.perform(delete("/api/answers/submission/sub-1"))
                .andExpect(status().isNoContent());
    }
}
