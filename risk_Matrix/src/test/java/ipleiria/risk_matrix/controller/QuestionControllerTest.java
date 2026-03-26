package ipleiria.risk_matrix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.dto.QuestionOptionDTO;
import ipleiria.risk_matrix.exceptions.exception.QuestionNotFoundException;
import ipleiria.risk_matrix.models.category.Category;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcTestConfig.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuestionService questionService;

    private Question question;
    private QuestionDTO questionDTO;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Network");

        question = new Question();
        question.setId(1L);
        question.setQuestionText("Do you have a firewall?");
        question.setCategory(category);
        question.setQuestionnaires(new ArrayList<>());
        question.setOptions(new ArrayList<>());

        QuestionOption opt = new QuestionOption();
        opt.setId(1L);
        opt.setOptionText("Yes");
        opt.setOptionType(OptionLevelType.IMPACT);
        opt.setOptionLevel(OptionLevel.LOW);
        opt.setQuestion(question);
        question.getOptions().add(opt);

        questionDTO = new QuestionDTO(question);
    }

    @Test
    void getAll_returnsList() throws Exception {
        when(questionService.getAllQuestions()).thenReturn(List.of(question));

        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].questionText", is("Do you have a firewall?")));
    }

    @Test
    void getById_existing_returns200() throws Exception {
        when(questionService.getQuestionDtoById(1L)).thenReturn(questionDTO);

        mockMvc.perform(get("/api/questions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionText", is("Do you have a firewall?")))
                .andExpect(jsonPath("$.categoryName", is("Network")));
    }

    @Test
    void getById_nonExisting_returns404() throws Exception {
        when(questionService.getQuestionDtoById(99L))
                .thenThrow(new QuestionNotFoundException("Question not found with id: 99"));

        mockMvc.perform(get("/api/questions/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByCategory_returnsList() throws Exception {
        when(questionService.getQuestionsByCategory("Network")).thenReturn(List.of(question));

        mockMvc.perform(get("/api/questions/category/Network"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void create_validInput_returns200() throws Exception {
        QuestionDTO input = new QuestionDTO();
        input.setQuestionText("New question?");
        input.setCategoryName("Network");
        input.setOptions(List.of(new QuestionOptionDTO("Yes", OptionLevelType.IMPACT, OptionLevel.LOW, null)));

        when(questionService.createQuestion(any(QuestionDTO.class))).thenReturn(questionDTO);

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionText", is("Do you have a firewall?")));
    }

    @Test
    void update_validInput_returns200() throws Exception {
        QuestionDTO input = new QuestionDTO();
        input.setQuestionText("Updated question?");
        input.setCategoryName("Network");
        input.setOptions(List.of(new QuestionOptionDTO("Yes", OptionLevelType.IMPACT, OptionLevel.LOW, null)));

        when(questionService.updateQuestion(eq(1L), any(QuestionDTO.class))).thenReturn(questionDTO);

        mockMvc.perform(put("/api/questions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_existing_returns200() throws Exception {
        doNothing().when(questionService).deleteQuestion(1L);

        mockMvc.perform(delete("/api/questions/1"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_nonExisting_returns404() throws Exception {
        doThrow(new QuestionNotFoundException("Question not found with id: 99"))
                .when(questionService).deleteQuestion(99L);

        mockMvc.perform(delete("/api/questions/99"))
                .andExpect(status().isNotFound());
    }
}
