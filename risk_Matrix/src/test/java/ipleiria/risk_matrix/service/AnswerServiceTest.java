package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.dto.UserAnswersDTO;
import ipleiria.risk_matrix.exceptions.exception.InvalidOptionException;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.category.Category;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.repository.AnswerRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private AnswerService answerService;

    private Question question;
    private QuestionOption yesOption;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Network");

        question = new Question();
        question.setId(10L);
        question.setQuestionText("Do you have a firewall?");
        question.setCategory(category);

        yesOption = new QuestionOption();
        yesOption.setId(100L);
        yesOption.setOptionText("Yes");
        yesOption.setOptionType(OptionLevelType.IMPACT);
        yesOption.setOptionLevel(OptionLevel.LOW);
        yesOption.setQuestion(question);

        question.setOptions(List.of(yesOption));
    }

    @Test
    void submitAnswer_validInput_returnsMappedDto() {
        AnswerDTO dto = new AnswerDTO();
        dto.setQuestionId(10L);
        dto.setUserResponse("Yes");
        dto.setEmail("user@example.com");

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(answerRepository.save(any(Answer.class))).thenAnswer(inv -> {
            Answer a = inv.getArgument(0);
            a.setId(1L);
            a.setCreatedAt(LocalDateTime.now());
            return a;
        });

        AnswerDTO result = answerService.submitAnswer(dto);

        assertThat(result.getQuestionId()).isEqualTo(10L);
        assertThat(result.getQuestionOptionId()).isEqualTo(100L);
        assertThat(result.getQuestionType()).isEqualTo(OptionLevelType.IMPACT);
        assertThat(result.getChosenLevel()).isEqualTo(OptionLevel.LOW);
    }

    @Test
    void submitAnswer_missingEmail_throwsIllegalArgument() {
        AnswerDTO dto = new AnswerDTO();
        dto.setQuestionId(10L);
        dto.setUserResponse("Yes");
        dto.setEmail("   ");

        assertThatThrownBy(() -> answerService.submitAnswer(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is required");
    }

    @Test
    void submitAnswer_invalidOption_throwsInvalidOptionException() {
        AnswerDTO dto = new AnswerDTO();
        dto.setQuestionId(10L);
        dto.setUserResponse("Invalid");
        dto.setEmail("user@example.com");

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> answerService.submitAnswer(dto))
                .isInstanceOf(InvalidOptionException.class)
                .hasMessageContaining("Invalid response");
    }

    @Test
    void submitAnswer_questionNotFound_throwsNotFoundException() {
        AnswerDTO dto = new AnswerDTO();
        dto.setQuestionId(999L);
        dto.setUserResponse("Yes");
        dto.setEmail("user@example.com");

        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerService.submitAnswer(dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Question not found");
    }

    @Test
    void submitMultipleAnswers_emptyList_throwsIllegalArgument() {
        assertThatThrownBy(() -> answerService.submitMultipleAnswers(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lista de respostas");
    }

    @Test
    void submitMultipleAnswers_assignsSingleSubmissionId() {
        AnswerDTO dto1 = new AnswerDTO();
        dto1.setQuestionId(10L);
        dto1.setUserResponse("Yes");
        dto1.setEmail("user@example.com");

        AnswerDTO dto2 = new AnswerDTO();
        dto2.setQuestionId(10L);
        dto2.setUserResponse("Yes");
        dto2.setEmail("user@example.com");

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(answerRepository.save(any(Answer.class))).thenAnswer(inv -> {
            Answer a = inv.getArgument(0);
            a.setId((long) (Math.random() * 1000));
            a.setCreatedAt(LocalDateTime.now());
            return a;
        });

        List<AnswerDTO> result = answerService.submitMultipleAnswers(List.of(dto1, dto2));

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getSubmissionId()).isEqualTo(result.getLast().getSubmissionId());
    }

    @Test
    void getAllAnswers_capsPageSizeAt500() {
        when(answerRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        answerService.getAllAnswers(0, 999);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(answerRepository).findAll(captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(500);
    }

    @Test
    void getAnswersByDateRange_endBeforeStart_throwsIllegalArgument() {
        LocalDate start = LocalDate.of(2025, 5, 10);
        LocalDate end = LocalDate.of(2025, 5, 9);

        assertThatThrownBy(() -> answerService.getAnswersByDateRange(start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date must not be before start date");
    }

    @Test
    void deleteSubmission_existing_deletesBySubmissionId() {
        String submissionId = "sub-1";
        Answer answer = new Answer();
        answer.setSubmissionId(submissionId);

        when(answerRepository.findBySubmissionId(submissionId)).thenReturn(List.of(answer));

        answerService.deleteSubmission(submissionId);

        verify(answerRepository).deleteBySubmissionId(submissionId);
    }

    @Test
    void deleteSubmission_missing_throwsNotFoundException() {
        when(answerRepository.findBySubmissionId("missing")).thenReturn(List.of());

        assertThatThrownBy(() -> answerService.deleteSubmission("missing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No answers found");
    }

    @Test
    void getUserSubmissionsWithSeverities_groupsBySubmission() {
        Answer a = new Answer();
        a.setId(1L);
        a.setQuestionId(10L);
        a.setQuestionText("Do you have a firewall?");
        a.setUserResponse("Yes");
        a.setQuestionType(OptionLevelType.IMPACT);
        a.setChosenLevel(OptionLevel.LOW);
        a.setEmail("user@example.com");
        a.setSubmissionId("sub-1");
        a.setCreatedAt(LocalDateTime.now());

        when(answerRepository.findByEmail("user@example.com")).thenReturn(List.of(a));
        when(questionRepository.findAllById(any())).thenReturn(List.of(question));

        List<UserAnswersDTO> result = answerService.getUserSubmissionsWithSeverities("user@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getSubmissionId()).isEqualTo("sub-1");
        assertThat(result.getFirst().getSeveritiesByCategory()).containsKey("Network");
    }
}
