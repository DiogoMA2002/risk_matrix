package ipleiria.risk_matrix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.dto.QuestionOptionDTO;
import ipleiria.risk_matrix.dto.QuestionnaireDTO;
import ipleiria.risk_matrix.models.category.Category;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.service.QuestionnaireService;
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

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuestionnaireController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcTestConfig.class)
class QuestionnaireControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuestionnaireService questionnaireService;

    private Questionnaire questionnaire;
    private QuestionnaireDTO questionnaireDTO;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Network");

        Question question = new Question();
        question.setId(10L);
        question.setQuestionText("Do you have firewall?");
        question.setCategory(category);

        QuestionOption option = new QuestionOption();
        option.setId(100L);
        option.setQuestion(question);
        option.setOptionText("Yes");
        option.setOptionType(OptionLevelType.IMPACT);
        option.setOptionLevel(OptionLevel.LOW);
        question.setOptions(List.of(option));

        questionnaire = new Questionnaire();
        questionnaire.setId(1L);
        questionnaire.setTitle("Assessment 1");
        questionnaire.setQuestions(List.of(question));

        questionnaireDTO = new QuestionnaireDTO(questionnaire);
    }

    @Test
    void createQuestionnaire_returns201() throws Exception {
        QuestionnaireDTO input = new QuestionnaireDTO();
        input.setTitle(" New Form ");

        when(questionnaireService.createQuestionnaire(any(Questionnaire.class))).thenReturn(questionnaire);

        mockMvc.perform(post("/api/questionnaires/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Assessment 1")));
    }

    @Test
    void getAllQuestionnaires_returns200() throws Exception {
        when(questionnaireService.getAllQuestionnaires()).thenReturn(List.of(questionnaire));

        mockMvc.perform(get("/api/questionnaires"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getQuestionnaireById_found_returns200() throws Exception {
        when(questionnaireService.getQuestionnaireById(1L)).thenReturn(Optional.of(questionnaire));

        mockMvc.perform(get("/api/questionnaires/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Assessment 1")));
    }

    @Test
    void exportQuestionnaire_returnsJsonAttachment() throws Exception {
        when(questionnaireService.getQuestionnaireById(1L)).thenReturn(Optional.of(questionnaire));

        mockMvc.perform(get("/api/questionnaires/1/export"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("questionnaire_1.json")))
                .andExpect(content().contentType("application/json; charset=UTF-8"));
    }

    @Test
    void updateQuestionnaire_returns200() throws Exception {
        Questionnaire updated = new Questionnaire();
        updated.setId(1L);
        updated.setTitle("Updated");
        updated.setQuestions(List.of());

        QuestionnaireDTO input = new QuestionnaireDTO();
        input.setTitle(" Updated ");

        when(questionnaireService.updateQuestionnaire(eq(1L), any(Questionnaire.class))).thenReturn(updated);

        mockMvc.perform(put("/api/questionnaires/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated")));
    }
}
