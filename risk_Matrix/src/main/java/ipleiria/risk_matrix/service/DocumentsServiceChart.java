package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.repository.AnswerRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ChartUtils;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import static ipleiria.risk_matrix.utils.RiskUtils.medianLevel;

@Service
public class DocumentsServiceChart {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;


    public DocumentsServiceChart(AnswerRepository answerRepository, QuestionRepository questionRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }

    public byte[] generateEnhancedDocx(String submissionId) throws IOException {
        List<Answer> answers = answerRepository.findBySubmissionId(submissionId);
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("No answers found for submission ID: " + submissionId);
        }

        Map<String, List<Answer>> answersByCategory = new HashMap<>();
        for (Answer ans : answers) {
            Optional<Question> questionOpt = questionRepository.findById(ans.getQuestionId());
            if (questionOpt.isEmpty() || questionOpt.get().getCategory() == null) continue;

            String category = questionOpt.get().getCategory().getName();
            answersByCategory.computeIfAbsent(category, _ -> new ArrayList<>()).add(ans);
        }

        Map<String, OptionLevel> impactLevels = new HashMap<>();
        Map<String, OptionLevel> probabilityLevels = new HashMap<>();

        for (Map.Entry<String, List<Answer>> entry : answersByCategory.entrySet()) {
            String category = entry.getKey();
            List<Answer> answersInCategory = entry.getValue();

            List<OptionLevel> impactValues = answersInCategory.stream()
                    .filter(a -> a.getQuestionType() == OptionLevelType.IMPACT)
                    .map(Answer::getChosenLevel)
                    .filter(Objects::nonNull)
                    .toList();

            List<OptionLevel> probabilityValues = answersInCategory.stream()
                    .filter(a -> a.getQuestionType() == OptionLevelType.PROBABILITY)
                    .map(Answer::getChosenLevel)
                    .filter(Objects::nonNull)
                    .toList();

            OptionLevel medianImpact = medianLevel(impactValues);
            OptionLevel medianProbability = medianLevel(probabilityValues);

            impactLevels.put(category, medianImpact != null ? medianImpact : OptionLevel.LOW);
            probabilityLevels.put(category, medianProbability != null ? medianProbability : OptionLevel.LOW);
        }

        try (InputStream template = getClass().getClassLoader().getResourceAsStream("template/template.docx");
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            if (template == null) {
                throw new FileNotFoundException("Template not found in classpath.");
            }

            XWPFDocument document = new XWPFDocument(template);
            Map<String, String> vars = new HashMap<>();
            Answer firstAnswer = answers.getFirst();
            vars.put("submissionId", submissionId);
            vars.put("email", firstAnswer.getEmail());
            vars.put("date", firstAnswer.getCreatedAt().toLocalDate().toString());

            replacePlaceholders(document, vars);
            addSummarySection(document, impactLevels, probabilityLevels);
            try {
                addRiskMatrixChart(document, impactLevels, probabilityLevels);
            } catch (InvalidFormatException e) {
                throw new RuntimeException(e);
            }
            addAnswersTable(document, answersByCategory);

            document.write(out);
            return out.toByteArray();
        }
    }

    private void replacePlaceholders(XWPFDocument doc, Map<String, String> replacements) {
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            StringBuilder fullText = new StringBuilder();
            for (XWPFRun run : paragraph.getRuns()) {
                String text = run.getText(0);
                if (text != null) {
                    fullText.append(text);
                }
            }

            String replaced = fullText.toString();
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                replaced = replaced.replace("${" + entry.getKey() + "}", entry.getValue());
            }

            for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }

            XWPFRun newRun = paragraph.createRun();
            newRun.setText(replaced);
            newRun.setFontFamily("Calibri");
        }
    }

    private void addSummarySection(XWPFDocument document, Map<String, OptionLevel> impacts, Map<String, OptionLevel> probabilities) {
        for (String category : impacts.keySet()) {
            XWPFParagraph p = document.createParagraph();
            XWPFRun run = p.createRun();
            run.setText(category + ": Impact = " + impacts.get(category) + ", Probability = " + probabilities.get(category));
            run.setFontSize(14);
            run.setFontFamily("Calibri");
            run.setBold(true);
        }
    }

    private void addRiskMatrixChart(XWPFDocument document, Map<String, OptionLevel> impacts, Map<String, OptionLevel> probabilities) throws IOException, InvalidFormatException {
        Map<OptionLevel, Integer> levelMap = Map.of(
                OptionLevel.LOW, 1,
                OptionLevel.MEDIUM, 2,
                OptionLevel.HIGH, 3
        );

        XYSeries series = new XYSeries("Risk Categories");
        for (String category : impacts.keySet()) {
            int x = levelMap.getOrDefault(probabilities.get(category), 1);
            int y = levelMap.getOrDefault(impacts.get(category), 1);
            series.add(x, y);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createScatterPlot(
                "Risk Matrix", "Probability", "Impact", dataset
        );

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(240, 240, 240));
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setRange(0.5, 3.5);
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0.5, 3.5);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // Add labels
        for (String category : impacts.keySet()) {
            int x = levelMap.getOrDefault(probabilities.get(category), 1);
            int y = levelMap.getOrDefault(impacts.get(category), 1);
            XYTextAnnotation label = new XYTextAnnotation(category, x + 0.05, y + 0.05);
            label.setFont(new Font("Arial", Font.PLAIN, 10));
            plot.addAnnotation(label);
        }

        File chartFile = File.createTempFile("risk_matrix_chart", ".png");
        ChartUtils.saveChartAsPNG(chartFile, chart, 400, 400);

        XWPFParagraph p = document.createParagraph();
        XWPFRun r = p.createRun();
        try (InputStream pic = new FileInputStream(chartFile)) {
            r.addPicture(pic, Document.PICTURE_TYPE_PNG, chartFile.getName(), Units.toEMU(400), Units.toEMU(400));
        }
        chartFile.deleteOnExit();
    }

    private void addAnswersTable(XWPFDocument document, Map<String, List<Answer>> answersByCategory) {
        for (Map.Entry<String, List<Answer>> entry : answersByCategory.entrySet()) {
            String category = entry.getKey();
            List<Answer> answers = entry.getValue();

            XWPFParagraph categoryHeader = document.createParagraph();
            XWPFRun headerRun = categoryHeader.createRun();
            headerRun.setText("Categoria: " + category);
            headerRun.setBold(true);
            headerRun.setFontSize(14);
            headerRun.setFontFamily("Calibri");

            XWPFTable table = document.createTable();
            XWPFTableRow header = table.getRow(0);
            header.getCell(0).setText("Pergunta");
            header.addNewTableCell().setText("Resposta");
            header.addNewTableCell().setText("Tipo");
            header.addNewTableCell().setText("Nível");
            header.addNewTableCell().setText("Recomendação"); // ✅ nova coluna

            for (Answer answer : answers) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(answer.getQuestionText());
                row.getCell(1).setText(answer.getUserResponse());
                row.getCell(2).setText(answer.getQuestionType() != null ? answer.getQuestionType().name() : "-");
                row.getCell(3).setText(answer.getChosenLevel() != null ? answer.getChosenLevel().name() : "-");
                String recommendation = "-";
                if (answer.getQuestionOptionId() != null) {
                    Optional<Question> questionOpt = questionRepository.findById(answer.getQuestionId());
                    if (questionOpt.isPresent()) {
                        recommendation = questionOpt.get().getOptions().stream()
                                .filter(opt -> opt.getId().equals(answer.getQuestionOptionId()))
                                .map(opt -> opt.getRecommendation() != null ? opt.getRecommendation() : "-")
                                .findFirst()
                                .orElse("-");
                    }
                }

                row.getCell(4).setText(recommendation);
            }

            document.createParagraph();
        }
    }
}