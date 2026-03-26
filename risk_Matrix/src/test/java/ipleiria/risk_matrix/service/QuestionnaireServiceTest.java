package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.exceptions.exception.QuestionnaireNotFoundException;
import ipleiria.risk_matrix.models.category.Category;
import ipleiria.risk_matrix.models.questionnaire.Questionnaire;
import ipleiria.risk_matrix.models.questions.Question;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionnaireServiceTest {

    @Mock
    private QuestionnaireRepository questionnaireRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private QuestionnaireService questionnaireService;

    private Questionnaire questionnaire;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Network");

        Question q1 = new Question();
        q1.setId(1L);
        q1.setQuestionText("Do you have a firewall?");
        q1.setCategory(category);
        q1.setQuestionnaires(new ArrayList<>());

        questionnaire = new Questionnaire();
        questionnaire.setId(1L);
        questionnaire.setTitle("Security Assessment");
        questionnaire.setQuestions(new ArrayList<>(List.of(q1)));
        q1.getQuestionnaires().add(questionnaire);
    }

    @Test
    void createQuestionnaire_savesAndReturns() {
        Questionnaire input = new Questionnaire();
        input.setTitle("New Questionnaire");

        when(questionnaireRepository.save(any(Questionnaire.class))).thenAnswer(inv -> {
            Questionnaire saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        Questionnaire result = questionnaireService.createQuestionnaire(input);

        assertThat(result.getTitle()).isEqualTo("New Questionnaire");
        verify(questionnaireRepository).save(input);
    }

    @Test
    void getAllQuestionnaires_returnsList() {
        when(questionnaireRepository.findAllWithDetails()).thenReturn(List.of(questionnaire));

        List<Questionnaire> result = questionnaireService.getAllQuestionnaires();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Security Assessment");
    }

    @Test
    void getQuestionnaireById_existing_returnsOptional() {
        when(questionnaireRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(questionnaire));

        Optional<Questionnaire> result = questionnaireService.getQuestionnaireById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Security Assessment");
    }

    @Test
    void getQuestionnaireById_nonExisting_returnsEmpty() {
        when(questionnaireRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        Optional<Questionnaire> result = questionnaireService.getQuestionnaireById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void deleteQuestionnaire_existing_deletesAndCleansOrphans() {
        when(questionnaireRepository.findById(1L)).thenReturn(Optional.of(questionnaire));
        when(questionRepository.findAllByQuestionnairesIsEmpty()).thenReturn(List.of());

        questionnaireService.deleteQuestionnaire(1L);

        verify(questionnaireRepository).delete(questionnaire);
        verify(questionRepository).findAllByQuestionnairesIsEmpty();
    }

    @Test
    void deleteQuestionnaire_nonExisting_throwsQuestionnaireNotFound() {
        when(questionnaireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionnaireService.deleteQuestionnaire(99L))
                .isInstanceOf(QuestionnaireNotFoundException.class)
                .hasMessageContaining("Questionnaire not found");
    }

    @Test
    void deleteQuestionnaire_withOrphans_deletesOrphanQuestions() {
        Question orphan = new Question();
        orphan.setId(10L);
        orphan.setQuestionText("Orphan question");
        orphan.setQuestionnaires(new ArrayList<>());

        when(questionnaireRepository.findById(1L)).thenReturn(Optional.of(questionnaire));
        when(questionRepository.findAllByQuestionnairesIsEmpty()).thenReturn(List.of(orphan));

        questionnaireService.deleteQuestionnaire(1L);

        verify(questionRepository).deleteAll(List.of(orphan));
    }

    @Test
    void getAllQuestionsForQuestionnaire_existing_returnsQuestions() {
        when(questionnaireRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(questionnaire));

        List<Question> result = questionnaireService.getAllQuestionsForQuestionnaire(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getQuestionText()).isEqualTo("Do you have a firewall?");
    }

    @Test
    void getAllQuestionsForQuestionnaire_nonExisting_throws() {
        when(questionnaireRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionnaireService.getAllQuestionsForQuestionnaire(99L))
                .isInstanceOf(QuestionnaireNotFoundException.class);
    }

    @Test
    void getQuestionsByCategory_filtersCorrectly() {
        Question q2 = new Question();
        q2.setId(2L);
        q2.setQuestionText("Different category question");
        Category otherCat = new Category();
        otherCat.setId(2L);
        otherCat.setName("Physical");
        q2.setCategory(otherCat);
        questionnaire.getQuestions().add(q2);

        when(questionnaireRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(questionnaire));

        List<Question> result = questionnaireService.getQuestionsByCategory(1L, "Network");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCategory().getName()).isEqualTo("Network");
    }

    @Test
    void searchQuestionnaires_withTitle_searchesByTitle() {
        when(questionnaireRepository.findByTitleContainingIgnoreCase("Security"))
                .thenReturn(List.of(questionnaire));

        List<Questionnaire> result = questionnaireService.searchQuestionnaires("Security");

        assertThat(result).hasSize(1);
    }

    @Test
    void searchQuestionnaires_nullTitle_returnsAll() {
        when(questionnaireRepository.findAll()).thenReturn(List.of(questionnaire));

        List<Questionnaire> result = questionnaireService.searchQuestionnaires(null);

        assertThat(result).hasSize(1);
        verify(questionnaireRepository).findAll();
    }

    @Test
    void searchQuestionnaires_blankTitle_returnsAll() {
        when(questionnaireRepository.findAll()).thenReturn(List.of(questionnaire));

        List<Questionnaire> result = questionnaireService.searchQuestionnaires("   ");

        assertThat(result).hasSize(1);
        verify(questionnaireRepository).findAll();
    }

    @Test
    void updateQuestionnaire_existing_updatesTitle() {
        Questionnaire updated = new Questionnaire();
        updated.setTitle("New Title");

        when(questionnaireRepository.findById(1L)).thenReturn(Optional.of(questionnaire));
        when(questionnaireRepository.save(any(Questionnaire.class))).thenAnswer(inv -> inv.getArgument(0));

        Questionnaire result = questionnaireService.updateQuestionnaire(1L, updated);

        assertThat(result.getTitle()).isEqualTo("New Title");
    }

    @Test
    void updateQuestionnaire_nonExisting_throws() {
        Questionnaire updated = new Questionnaire();
        updated.setTitle("Anything");

        when(questionnaireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionnaireService.updateQuestionnaire(99L, updated))
                .isInstanceOf(QuestionnaireNotFoundException.class);
    }
}
