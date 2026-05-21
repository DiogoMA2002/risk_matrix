package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.Severity;
import ipleiria.risk_matrix.repository.AnswerRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import ipleiria.risk_matrix.utils.documents.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static ipleiria.risk_matrix.utils.RiskUtils.computeCategoryScore;
import static ipleiria.risk_matrix.utils.RiskUtils.computeCategorySeverity;
import static ipleiria.risk_matrix.utils.documents.ReportPresentation.FONT;

@Service
public class DocumentsService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentsService.class);

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    public DocumentsService(AnswerRepository answerRepository, QuestionRepository questionRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generateEnhancedDocx(String submissionId) throws IOException {
        List<Answer> answers = answerRepository.findBySubmissionId(submissionId);
        if (answers == null || answers.isEmpty()) {
            throw new NotFoundException("No answers found for submission ID: " + submissionId);
        }

        Set<Long> questionIds = answers.stream()
                .map(Answer::getQuestionId)
                .collect(Collectors.toSet());
        Map<Long, Question> questionMap = questionRepository.findAllById(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        ReportAnswerGrouper.GroupingResult grouping =
                ReportAnswerGrouper.groupByCategory(answers, questionMap);
        Map<String, List<Answer>> answersByCategory = grouping.answersByCategory();
        List<SkippedAnswer> skippedAnswers = grouping.skippedAnswers();

        if (!skippedAnswers.isEmpty()) {
            logger.warn("Submission {} skipped {} answers during report generation",
                    submissionId, skippedAnswers.size());
        }

        if (answersByCategory.isEmpty()) {
            throw new NotFoundException(
                    "No categorizable answers found for submission ID: " + submissionId);
        }

        Map<String, Severity> severities = new HashMap<>();
        Map<String, Integer> categoryScores = new HashMap<>();
        for (Map.Entry<String, List<Answer>> entry : answersByCategory.entrySet()) {
            List<AnswerDTO> dtos = entry.getValue().stream()
                    .map(AnswerDTO::new)
                    .toList();
            severities.put(entry.getKey(), computeCategorySeverity(dtos));
            categoryScores.put(entry.getKey(), computeCategoryScore(dtos));
        }

        double globalIndex = categoryScores.values().stream()
                .filter(score -> score > 0)
                .mapToDouble(ReportPresentation::normalizedScorePercent)
                .average()
                .orElse(0.0);

        try (InputStream template = getClass().getClassLoader().getResourceAsStream("template/template.docx");
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            if (template == null) {
                throw new FileNotFoundException("Template not found in classpath.");
            }

            XWPFDocument document = new XWPFDocument(template);
            Answer firstAnswer = answers.getFirst();
            Map<String, String> vars = Map.of(
                    "submissionId", submissionId,
                    "email", firstAnswer.getEmail(),
                    "date", firstAnswer.getCreatedAt().toLocalDate().toString()
            );

            DocumentPlaceholderReplacer.replaceAll(document, vars);
            ReportContentBuilder.addSkippedAnswersSection(document, skippedAnswers);

            ReportContentBuilder.addSummaryIntro(document);
            addSeverityChart(document, severities);
            ReportContentBuilder.addPageBreak(document);
            ReportContentBuilder.addSummarySeverityList(document, severities);

            ReportContentBuilder.addQuantitativeSection(document, categoryScores, globalIndex, severities);
            addCategoryScoreChart(document, categoryScores);

            ReportContentBuilder.addPageBreak(document);
            ReportContentBuilder.addAnswersTable(document, answersByCategory, severities, questionMap);

            document.write(out);
            return out.toByteArray();
        }
    }

    private void addSeverityChart(XWPFDocument document, Map<String, Severity> severities) {
        try {
            DocumentChartBuilder.addSeverityPieChart(document, severities);
        } catch (IOException | org.apache.poi.openxml4j.exceptions.InvalidFormatException e) {
            logger.warn("Error creating severity pie chart: {}", e.getMessage());
            addChartFailureNote(document,
                    "[Nota: O gráfico de distribuição por severidade não pôde ser gerado.]");
        }
    }

    private void addCategoryScoreChart(XWPFDocument document, Map<String, Integer> categoryScores) {
        try {
            DocumentChartBuilder.addCategoryScoreBarChart(document, categoryScores);
        } catch (IOException | org.apache.poi.openxml4j.exceptions.InvalidFormatException e) {
            logger.warn("Error creating category bar chart: {}", e.getMessage());
            addChartFailureNote(document,
                    "[Nota: O gráfico de pontuação por categoria não pôde ser gerado.]");
        }
    }

    private void addChartFailureNote(XWPFDocument document, String message) {
        XWPFParagraph note = document.createParagraph();
        XWPFRun noteRun = note.createRun();
        noteRun.setFontFamily(FONT);
        noteRun.setFontSize(10);
        noteRun.setItalic(true);
        noteRun.setColor("FF0000");
        noteRun.setText(message);
    }
}
