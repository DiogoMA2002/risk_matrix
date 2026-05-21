package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.category.Category;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.repository.AnswerRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentsServiceTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private DocumentsService documentsService;

    @BeforeEach
    void setUpTemplate() throws Exception {
        URL rootUrl = Thread.currentThread().getContextClassLoader().getResource("");
        assertThat(rootUrl).isNotNull();

        Path classpathRoot = Path.of(rootUrl.toURI());
        Path templatePath = classpathRoot.resolve("template").resolve("template.docx");
        Files.createDirectories(templatePath.getParent());

        if (!Files.exists(templatePath)) {
            try (XWPFDocument doc = new XWPFDocument();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                doc.createParagraph().createRun().setText("Submission ${submissionId}");
                doc.createParagraph().createRun().setText("Email ${email}");
                doc.createParagraph().createRun().setText("Date ${date}");
                doc.write(out);
                Files.write(templatePath, out.toByteArray());
            }
        }
    }

    @Test
    void generateEnhancedDocx_noAnswers_throwsNotFound() {
        when(answerRepository.findBySubmissionId("sub-404")).thenReturn(List.of());

        assertThatThrownBy(() -> documentsService.generateEnhancedDocx("sub-404"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No answers found for submission ID");
    }

    @Test
    void generateEnhancedDocx_validSubmission_returnsDocxAndReplacesPlaceholders() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Network Security");

        QuestionOption option = new QuestionOption();
        option.setId(200L);
        option.setOptionText("Yes");
        option.setOptionType(OptionLevelType.IMPACT);
        option.setOptionLevel(OptionLevel.HIGH);
        option.setRecommendation("Enable MFA for all users");

        Question question = new Question();
        question.setId(100L);
        question.setQuestionText("Do you enforce MFA?");
        question.setCategory(category);
        question.setOptions(List.of(option));

        Answer answer = new Answer();
        answer.setId(1L);
        answer.setQuestionId(100L);
        answer.setQuestionOptionId(200L);
        answer.setQuestionText("Do you enforce MFA?");
        answer.setUserResponse("Yes");
        answer.setQuestionType(OptionLevelType.IMPACT);
        answer.setChosenLevel(OptionLevel.HIGH);
        answer.setEmail("user@example.com");
        answer.setSubmissionId("sub-1");
        answer.setCreatedAt(LocalDateTime.of(2026, 3, 26, 12, 0));

        when(answerRepository.findBySubmissionId("sub-1")).thenReturn(List.of(answer));
        when(questionRepository.findAllById(any())).thenReturn(List.of(question));

        byte[] bytes = documentsService.generateEnhancedDocx("sub-1");

        assertThat(bytes).isNotEmpty();
        verify(questionRepository).findAllById(any());
        verify(answerRepository, never()).deleteAll(any());

        try (XWPFDocument result = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            String fullText = result.getParagraphs().stream()
                    .map(p -> p.getText() == null ? "" : p.getText())
                    .reduce("", (a, b) -> a + "\n" + b);
            assertThat(fullText).contains("sub-1");
            assertThat(fullText).contains("user@example.com");
            assertThat(fullText).contains("2026-03-26");
        }
    }

    @Test
    void generateEnhancedDocx_allAnswersSkipped_throwsNotFound() {
        Answer answer1 = new Answer();
        answer1.setId(1L);
        answer1.setQuestionId(100L);
        answer1.setQuestionText("Question A");
        answer1.setUserResponse("Yes");
        answer1.setEmail("user@example.com");
        answer1.setSubmissionId("sub-ignored");
        answer1.setCreatedAt(LocalDateTime.of(2026, 3, 26, 12, 0));

        Answer answer2 = new Answer();
        answer2.setId(2L);
        answer2.setQuestionId(101L);
        answer2.setQuestionText("Question B");
        answer2.setUserResponse("No");
        answer2.setEmail("user@example.com");
        answer2.setSubmissionId("sub-ignored");
        answer2.setCreatedAt(LocalDateTime.of(2026, 3, 26, 12, 0));

        Question noCategoryQuestion = new Question();
        noCategoryQuestion.setId(100L);
        noCategoryQuestion.setQuestionText("Question A");
        noCategoryQuestion.setCategory(null);
        noCategoryQuestion.setOptions(List.of());

        when(answerRepository.findBySubmissionId("sub-ignored")).thenReturn(List.of(answer1, answer2));
        when(questionRepository.findAllById(any())).thenReturn(List.of(noCategoryQuestion));

        assertThatThrownBy(() -> documentsService.generateEnhancedDocx("sub-ignored"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No categorizable answers found");
    }

    @Test
    void generateEnhancedDocx_partialSkippedAnswers_includesSkippedSection() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Identity");

        Question validQuestion = new Question();
        validQuestion.setId(100L);
        validQuestion.setQuestionText("Valid question");
        validQuestion.setCategory(category);
        validQuestion.setOptions(List.of());

        Answer validAnswer = new Answer();
        validAnswer.setId(1L);
        validAnswer.setQuestionId(100L);
        validAnswer.setQuestionText("Valid question");
        validAnswer.setUserResponse("Yes");
        validAnswer.setEmail("user@example.com");
        validAnswer.setSubmissionId("sub-partial");
        validAnswer.setCreatedAt(LocalDateTime.of(2026, 3, 26, 12, 0));

        Answer skippedAnswer = new Answer();
        skippedAnswer.setId(2L);
        skippedAnswer.setQuestionId(999L);
        skippedAnswer.setQuestionText("Missing question");
        skippedAnswer.setUserResponse("No");
        skippedAnswer.setEmail("user@example.com");
        skippedAnswer.setSubmissionId("sub-partial");
        skippedAnswer.setCreatedAt(LocalDateTime.of(2026, 3, 26, 12, 0));

        when(answerRepository.findBySubmissionId("sub-partial")).thenReturn(List.of(validAnswer, skippedAnswer));
        when(questionRepository.findAllById(any())).thenReturn(List.of(validQuestion));

        byte[] bytes = documentsService.generateEnhancedDocx("sub-partial");
        assertThat(bytes).isNotEmpty();

        try (XWPFDocument result = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            String fullText = result.getParagraphs().stream()
                    .map(p -> p.getText() == null ? "" : p.getText())
                    .reduce("", (a, b) -> a + "\n" + b);
            assertThat(fullText).contains("Respostas não incluídas no relatório");
            assertThat(fullText).contains("Missing question");
        }
    }

    @Test
    void generateEnhancedDocx_whenRecommendationResolvedByUserResponse_includesRecommendation() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Identity");

        QuestionOption option = new QuestionOption();
        option.setId(200L);
        option.setOptionText("Sometimes");
        option.setOptionType(OptionLevelType.IMPACT);
        option.setOptionLevel(OptionLevel.MEDIUM);
        option.setRecommendation("Rotate credentials quarterly");

        Question question = new Question();
        question.setId(100L);
        question.setQuestionText("Do you rotate credentials?");
        question.setCategory(category);
        question.setOptions(List.of(option));

        Answer answer = new Answer();
        answer.setId(1L);
        answer.setQuestionId(100L);
        answer.setQuestionOptionId(null);
        answer.setQuestionText("Do you rotate credentials?");
        answer.setUserResponse("Sometimes");
        answer.setQuestionType(OptionLevelType.IMPACT);
        answer.setChosenLevel(OptionLevel.MEDIUM);
        answer.setEmail("user@example.com");
        answer.setSubmissionId("sub-rec");
        answer.setCreatedAt(LocalDateTime.of(2026, 3, 26, 12, 0));

        when(answerRepository.findBySubmissionId("sub-rec")).thenReturn(List.of(answer));
        when(questionRepository.findAllById(any())).thenReturn(List.of(question));

        byte[] bytes = documentsService.generateEnhancedDocx("sub-rec");
        assertThat(bytes).isNotEmpty();

        try (XWPFDocument result = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            boolean hasRecommendation = result.getTables().stream()
                    .flatMap(t -> t.getRows().stream())
                    .flatMap(r -> r.getTableCells().stream())
                    .anyMatch(c -> "Rotate credentials quarterly".equals(c.getText()));
            assertThat(hasRecommendation).isTrue();
        }
    }

    @Test
    void generateEnhancedDocx_whenRecommendationCannotBeResolved_usesDashFallback() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Identity");

        Question question = new Question();
        question.setId(100L);
        question.setQuestionText("Do you rotate credentials?");
        question.setCategory(category);
        question.setOptions(List.of());

        Answer answer = new Answer();
        answer.setId(1L);
        answer.setQuestionId(100L);
        answer.setQuestionOptionId(null);
        answer.setQuestionText("Do you rotate credentials?");
        answer.setUserResponse("Sometimes");
        answer.setQuestionType(null);
        answer.setChosenLevel(null);
        answer.setEmail("user@example.com");
        answer.setSubmissionId("sub-fallback");
        answer.setCreatedAt(LocalDateTime.of(2026, 3, 26, 12, 0));

        when(answerRepository.findBySubmissionId("sub-fallback")).thenReturn(List.of(answer));
        when(questionRepository.findAllById(any())).thenReturn(List.of(question));

        byte[] bytes = documentsService.generateEnhancedDocx("sub-fallback");
        assertThat(bytes).isNotEmpty();

        try (XWPFDocument result = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            assertThat(result.getTables()).isNotEmpty();
            boolean hasFallbackDash = result.getTables().stream()
                    .flatMap(t -> t.getRows().stream())
                    .flatMap(r -> r.getTableCells().stream())
                    .anyMatch(c -> "-".equals(c.getText()));
            assertThat(hasFallbackDash).isTrue();
        }
    }
}
