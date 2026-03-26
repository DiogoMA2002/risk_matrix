package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.dto.QuestionOptionDTO;
import ipleiria.risk_matrix.exceptions.exception.InvalidCategoryException;
import ipleiria.risk_matrix.exceptions.exception.QuestionNotFoundException;
import ipleiria.risk_matrix.exceptions.exception.QuestionnaireNotFoundException;
import ipleiria.risk_matrix.models.category.Category;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.repository.CategoryRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import ipleiria.risk_matrix.repository.QuestionnaireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionnaireRepository questionnaireRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private QuestionService questionService;

    private Category category;
    private Question question;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Network");

        question = new Question();
        question.setId(1L);
        question.setQuestionText("Do you have a firewall?");
        question.setCategory(category);
        question.setOptions(new ArrayList<>());
        question.setQuestionnaires(new ArrayList<>());

        QuestionOption option = new QuestionOption();
        option.setId(1L);
        option.setOptionText("Yes");
        option.setOptionType(OptionLevelType.IMPACT);
        option.setOptionLevel(OptionLevel.LOW);
        option.setQuestion(question);
        question.getOptions().add(option);
    }

    @Test
    void getAllQuestions_returnsList() {
        when(questionRepository.findAllWithDetails()).thenReturn(List.of(question));

        List<Question> result = questionService.getAllQuestions();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getQuestionText()).isEqualTo("Do you have a firewall?");
    }

    @Test
    void getQuestionsByCategory_validCategory_returnsFiltered() {
        when(questionRepository.findByCategoryNameIgnoreCase("Network")).thenReturn(List.of(question));

        List<Question> result = questionService.getQuestionsByCategory("Network");

        assertThat(result).hasSize(1);
    }

    @Test
    void getQuestionsByCategory_nullCategory_returnsEmpty() {
        List<Question> result = questionService.getQuestionsByCategory(null);

        assertThat(result).isEmpty();
    }

    @Test
    void getQuestionsByCategory_blankCategory_returnsEmpty() {
        List<Question> result = questionService.getQuestionsByCategory("   ");

        assertThat(result).isEmpty();
    }

    @Test
    void getQuestionById_existing_returns() {
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        Question result = questionService.getQuestionById(1L);

        assertThat(result.getQuestionText()).isEqualTo("Do you have a firewall?");
    }

    @Test
    void getQuestionById_nonExisting_throwsQuestionNotFound() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.getQuestionById(99L))
                .isInstanceOf(QuestionNotFoundException.class)
                .hasMessageContaining("Question not found");
    }

    @Test
    void getQuestionDtoById_returnsDTO() {
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        QuestionDTO result = questionService.getQuestionDtoById(1L);

        assertThat(result.getQuestionText()).isEqualTo("Do you have a firewall?");
        assertThat(result.getCategoryName()).isEqualTo("Network");
    }

    @Test
    void deleteQuestion_existing_removesFromQuestionnairesAndDeletes() {
        Questionnaire q = new Questionnaire();
        q.setId(1L);
        q.setQuestions(new ArrayList<>(List.of(question)));
        question.getQuestionnaires().add(q);

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        questionService.deleteQuestion(1L);

        assertThat(q.getQuestions()).doesNotContain(question);
        assertThat(question.getQuestionnaires()).isEmpty();
        verify(questionRepository).delete(question);
    }

    @Test
    void deleteQuestion_nonExisting_throws() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.deleteQuestion(99L))
                .isInstanceOf(QuestionNotFoundException.class);
    }

    @Test
    void createQuestion_nullDto_throwsNullPointer() {
        assertThatThrownBy(() -> questionService.createQuestion(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createQuestion_nullCategory_throwsInvalidCategory() {
        QuestionDTO dto = new QuestionDTO();
        dto.setQuestionText("Some question?");
        dto.setCategoryName(null);
        dto.setOptions(List.of(createOptionDTO()));

        when(questionRepository.findByQuestionTextAndCategory_Name("Some question?", null))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.createQuestion(dto))
                .isInstanceOf(InvalidCategoryException.class);
    }

    @Test
    void createQuestion_existingQuestion_reusesAndAssociates() {
        QuestionDTO dto = new QuestionDTO();
        dto.setQuestionText("Do you have a firewall?");
        dto.setCategoryName("Network");
        dto.setQuestionnaireIds(List.of());

        when(questionRepository.findByQuestionTextAndCategory_Name("Do you have a firewall?", "Network"))
                .thenReturn(Optional.of(question));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        QuestionDTO result = questionService.createQuestion(dto);

        assertThat(result.getQuestionText()).isEqualTo("Do you have a firewall?");
    }

    @Test
    void createQuestion_withQuestionnaire_associatesCorrectly() {
        Questionnaire q = new Questionnaire();
        q.setId(1L);
        q.setQuestions(new ArrayList<>());

        QuestionDTO dto = new QuestionDTO();
        dto.setQuestionText("Do you have a firewall?");
        dto.setCategoryName("Network");
        dto.setQuestionnaireIds(List.of(1L));

        when(questionRepository.findByQuestionTextAndCategory_Name("Do you have a firewall?", "Network"))
                .thenReturn(Optional.of(question));
        when(questionnaireRepository.findById(1L)).thenReturn(Optional.of(q));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        questionService.createQuestion(dto);

        assertThat(question.getQuestionnaires()).contains(q);
        assertThat(q.getQuestions()).contains(question);
    }

    @Test
    void createQuestion_withInvalidQuestionnaire_throws() {
        QuestionDTO dto = new QuestionDTO();
        dto.setQuestionText("Do you have a firewall?");
        dto.setCategoryName("Network");
        dto.setQuestionnaireIds(List.of(999L));

        when(questionRepository.findByQuestionTextAndCategory_Name("Do you have a firewall?", "Network"))
                .thenReturn(Optional.of(question));
        when(questionnaireRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.createQuestion(dto))
                .isInstanceOf(QuestionnaireNotFoundException.class);
    }

    private QuestionOptionDTO createOptionDTO() {
        QuestionOptionDTO opt = new QuestionOptionDTO();
        opt.setOptionText("Yes");
        opt.setOptionType(OptionLevelType.IMPACT);
        opt.setOptionLevel(OptionLevel.LOW);
        return opt;
    }
}
