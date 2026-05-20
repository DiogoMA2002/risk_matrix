package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.Severity;
import ipleiria.risk_matrix.repository.AnswerRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;
import org.apache.poi.xddf.usermodel.chart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;
import org.openxmlformats.schemas.drawingml.x2006.main.*;

import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static ipleiria.risk_matrix.utils.RiskUtils.computeCategorySeverity;
import static ipleiria.risk_matrix.utils.RiskUtils.computeCategoryScore;

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
            throw new IllegalArgumentException("No answers found for submission ID: " + submissionId);
        }

        // Pre-fetch all referenced questions in one query to avoid N+1 per-answer lookups.
        Set<Long> questionIds = answers.stream()
                .map(Answer::getQuestionId)
                .collect(Collectors.toSet());
        Map<Long, Question> questionMap = questionRepository.findAllById(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        Map<String, List<Answer>> answersByCategory = new HashMap<>();
        for (Answer ans : answers) {
            Question q = questionMap.get(ans.getQuestionId());
            if (q == null || q.getCategory() == null) continue;
            String category = q.getCategory().getName();
            answersByCategory.computeIfAbsent(category, _ -> new ArrayList<>()).add(ans);
        }

        Map<String, Severity> severities = new HashMap<>();
        Map<String, Integer> categoryScores = new HashMap<>();
        for (Map.Entry<String, List<Answer>> entry : answersByCategory.entrySet()) {
            List<AnswerDTO> dtos = entry.getValue().stream()
                    .map(AnswerDTO::new)
                    .collect(Collectors.toList());
            severities.put(entry.getKey(), computeCategorySeverity(dtos));
            categoryScores.put(entry.getKey(), computeCategoryScore(dtos));
        }

        // Global risk index: average of per-category normalized scores (0–100), excluding UNKNOWN categories.
        double globalIndex = categoryScores.values().stream()
                .filter(s -> s > 0)
                .mapToDouble(s -> (s - 1.0) / 8.0 * 100.0)
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

            replacePlaceholders(document, vars);
            addSummarySection(document, severities);
            addQuantitativeSection(document, categoryScores, globalIndex, severities);
            addPageBreak(document);
            addAnswersTable(document, answersByCategory, severities, questionMap);

            document.write(out);
            return out.toByteArray();
        }
    }

    // Replaces ${key} placeholders in template paragraphs while preserving run formatting.
    // Only paragraphs that actually contain a placeholder are modified.
    private void replacePlaceholders(XWPFDocument doc, Map<String, String> replacements) {
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            List<XWPFRun> runs = paragraph.getRuns();
            if (runs.isEmpty()) continue;

            StringBuilder fullText = new StringBuilder();
            for (XWPFRun run : runs) {
                String text = run.getText(0);
                if (text != null) fullText.append(text);
            }

            String original = fullText.toString();
            String replaced = original;
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                replaced = replaced.replace("${" + entry.getKey() + "}", entry.getValue());
            }

            if (original.equals(replaced)) continue;

            // Preserve the first run's formatting; set new text on it; remove remaining runs.
            runs.get(0).setText(replaced, 0);
            for (int i = runs.size() - 1; i >= 1; i--) {
                paragraph.removeRun(i);
            }
        }
    }

    private void addSummarySection(XWPFDocument document, Map<String, Severity> severities) {
        XWPFParagraph summary = document.createParagraph();
        XWPFRun textRun = summary.createRun();
        textRun.setFontSize(11);
        textRun.setFontFamily("Verdana");
        textRun.setText("Este relatório apresenta os resultados da avaliação de risco realizada com base nas respostas submetidas. Foram analisadas várias áreas críticas de segurança da informação, como autenticação, backups, rede e acesso remoto. Abaixo apresenta-se um resumo das categorias avaliadas e os respetivos níveis de risco atribuídos:");

        try {
            addPieChart(document, severities);
        } catch (IOException | org.apache.poi.openxml4j.exceptions.InvalidFormatException e) {
            logger.warn("Error creating pie chart: {}", e.getMessage());
            XWPFParagraph note = document.createParagraph();
            XWPFRun noteRun = note.createRun();
            noteRun.setFontFamily("Verdana");
            noteRun.setFontSize(10);
            noteRun.setItalic(true);
            noteRun.setColor("FF0000");
            noteRun.setText("[Nota: O gráfico de distribuição não pôde ser gerado.]");
        }

        addPageBreak(document);

        // Sort by severity level descending (CRITICAL=4 first, UNKNOWN=0 last).
        severities.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Severity>>comparingInt(e -> getSeverityLevel(e.getValue()))
                        .reversed())
                .forEach(entry -> {
                    XWPFParagraph p = document.createParagraph();

                    XWPFRun catRun = p.createRun();
                    catRun.setFontSize(12);
                    catRun.setFontFamily("Verdana");
                    catRun.setText("- " + entry.getKey() + ": ");

                    int severityLevel = getSeverityLevel(entry.getValue());
                    XWPFRun sevRun = p.createRun();
                    sevRun.setFontSize(12);
                    sevRun.setFontFamily("Verdana");
                    sevRun.setBold(true);
                    sevRun.setColor(severityColorHex(entry.getValue()));
                    if (severityLevel > 0) {
                        sevRun.setText(getSeverityDisplayName(entry.getValue()) + " (" + severityLevel + ")");
                    } else {
                        sevRun.setText(getSeverityDisplayName(entry.getValue()));
                    }
                });

        XWPFParagraph levelExplanation = document.createParagraph();
        XWPFRun explanationRun = levelExplanation.createRun();
        explanationRun.setFontSize(10);
        explanationRun.setFontFamily("Verdana");
        explanationRun.setItalic(true);
        explanationRun.setText("Nota: Os níveis numéricos entre parênteses referem-se à criticidade do risco: (1) = Baixo, (2) = Médio, (3) = Alto, (4) = Crítico.");

        XWPFParagraph outro = document.createParagraph();
        XWPFRun outroRun = outro.createRun();
        outroRun.setFontSize(11);
        outroRun.setFontFamily("Verdana");
        outroRun.setText("Recomenda-se a priorização das categorias com risco mais elevado. As recomendações específicas estão detalhadas por domínio no relatório abaixo, e visam mitigar vulnerabilidades identificadas com base em boas práticas de cibersegurança adaptadas.");

        addPageBreak(document);
    }

    private void addQuantitativeSection(XWPFDocument document, Map<String, Integer> categoryScores,
                                        double globalIndex, Map<String, Severity> severities) {
        XWPFParagraph titlePara = document.createParagraph();
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(14);
        titleRun.setFontFamily("Calibri");
        titleRun.setText("Análise Quantitativa");

        boolean hasValidScores = categoryScores.values().stream().anyMatch(s -> s > 0);

        XWPFParagraph indexPara = document.createParagraph();
        XWPFRun labelRun = indexPara.createRun();
        labelRun.setFontSize(12);
        labelRun.setFontFamily("Verdana");
        labelRun.setText("Índice de Risco Global: ");

        XWPFRun indexRun = indexPara.createRun();
        indexRun.setBold(true);
        indexRun.setFontSize(12);
        indexRun.setFontFamily("Verdana");
        if (hasValidScores) {
            indexRun.setText(String.format("%.0f / 100", globalIndex));
            indexRun.setColor(indexColorHex(globalIndex));
        } else {
            indexRun.setText("N/D");
            indexRun.setColor("808080");
        }

        XWPFParagraph notePara = document.createParagraph();
        XWPFRun noteRun = notePara.createRun();
        noteRun.setFontSize(9);
        noteRun.setFontFamily("Verdana");
        noteRun.setItalic(true);
        noteRun.setText("Nota: A pontuação por categoria (1–9) é o produto das medianas de impacto e probabilidade. O índice global (0–100) é a média normalizada das pontuações válidas, categorias sem dados suficientes são excluídas do cálculo (DESCONHECIDO).");

        document.createParagraph();

        XWPFTable table = document.createTable();
        XWPFTableRow header = table.getRow(0);
        header.getCell(0).setText("Categoria");
        header.addNewTableCell().setText("Pontuação (1–9)");
        header.addNewTableCell().setText("Índice (%)");
        header.addNewTableCell().setText("Severidade");

        categoryScores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    String category = entry.getKey();
                    int score = entry.getValue();
                    Severity severity = severities.getOrDefault(category, Severity.UNKNOWN);

                    XWPFTableRow row = table.createRow();
                    row.getCell(0).setText(category);

                    XWPFTableCell scoreCell = row.getCell(1);
                    XWPFTableCell indexCell = row.getCell(2);
                    if (score > 0) {
                        scoreCell.setText(String.valueOf(score));
                        scoreCell.setColor(scoreColorHex(score));
                        double pct = (score - 1.0) / 8.0 * 100.0;
                        indexCell.setText(String.format("%.0f%%", pct));
                        indexCell.setColor(scoreColorHex(score));
                    } else {
                        scoreCell.setText("-");
                        indexCell.setText("-");
                    }

                    XWPFTableCell severityCell = row.getCell(3);
                    severityCell.setText(getSeverityDisplayName(severity));
                    severityCell.setColor(severityColorHex(severity));
                });
    }

    private String getOptionLevelDisplayName(OptionLevel level) {
        if (level == null) return "-";
        return switch (level) {
            case HIGH   -> "ALTO";
            case MEDIUM -> "MÉDIO";
            case LOW    -> "BAIXO";
        };
    }

    private void addPieChart(XWPFDocument doc, Map<String, Severity> severities)
            throws IOException, org.apache.poi.openxml4j.exceptions.InvalidFormatException {

        // Group by Severity enum to keep colour mapping type-safe.
        Map<Severity, Long> countsBySeverity = severities.values().stream()
                .collect(Collectors.groupingBy(s -> s, LinkedHashMap::new, Collectors.counting()));
        long total = Math.max(1, countsBySeverity.values().stream().mapToLong(Long::longValue).sum());

        List<Severity> severityKeys = new ArrayList<>(countsBySeverity.keySet());
        List<String> labelsWithPct = severityKeys.stream()
                .map(s -> {
                    long c = countsBySeverity.get(s);
                    double pct = 100.0 * c / total;
                    return String.format("%s (%.0f%%)", getSeverityDisplayName(s), pct);
                })
                .toList();
        Double[] values = severityKeys.stream()
                .map(s -> countsBySeverity.get(s).doubleValue())
                .toArray(Double[]::new);

        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = title.createRun();
        r.setBold(true);
        r.setFontFamily("Calibri");
        r.setFontSize(14);
        r.setText("Distribuição por Categoria");

        final int W = (int) (17.5 * Units.EMU_PER_CENTIMETER);
        final int H = (int) (10.5 * Units.EMU_PER_CENTIMETER);
        XWPFChart chart = doc.createChart(W, H);
        setChartBackgroundWhite(chart);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.RIGHT);

        XDDFDataSource<String> dsLabels = XDDFDataSourcesFactory.fromArray(labelsWithPct.toArray(String[]::new));
        XDDFNumericalDataSource<Double> dsValues = XDDFDataSourcesFactory.fromArray(values);

        XDDFPieChartData data = (XDDFPieChartData) chart.createData(ChartTypes.PIE, null, null);
        XDDFPieChartData.Series series = (XDDFPieChartData.Series) data.addSeries(dsLabels, dsValues);
        series.setShowLeaderLines(true);
        data.setVaryColors(false);
        chart.plot(data);

        CTPieChart ctPie = chart.getCTChart().getPlotArea().getPieChartArray(0);
        CTDLbls dLbls = ctPie.isSetDLbls() ? ctPie.getDLbls() : ctPie.addNewDLbls();
        dLbls.addNewShowLegendKey().setVal(false);
        dLbls.addNewShowVal().setVal(false);
        dLbls.addNewShowCatName().setVal(false);
        dLbls.addNewShowSerName().setVal(false);
        dLbls.addNewShowPercent().setVal(true);

        for (int i = 0; i < severityKeys.size(); i++) {
            int[] rgb = colourForSeverity(severityKeys.get(i));
            setPieSliceRgb(chart, i, rgb[0], rgb[1], rgb[2]);
        }
    }

    private void setSolidFillRgb(CTShapeProperties spPr) {
        CTSolidColorFillProperties solid = spPr.isSetSolidFill() ? spPr.getSolidFill() : spPr.addNewSolidFill();
        CTSRgbColor rgb = solid.isSetSrgbClr() ? solid.getSrgbClr() : solid.addNewSrgbClr();
        rgb.setVal(new byte[]{(byte) 255, (byte) 255, (byte) 255});
    }

    private int[] colourForSeverity(Severity severity) {
        return switch (severity) {
            case CRITICAL -> new int[]{139, 0, 0};
            case HIGH     -> new int[]{255, 0, 0};
            case MEDIUM   -> new int[]{255, 165, 0};
            case LOW      -> new int[]{0, 128, 0};
            case UNKNOWN  -> new int[]{128, 128, 128};
        };
    }

    private String severityColorHex(Severity severity) {
        return switch (severity) {
            case CRITICAL -> "8B0000";
            case HIGH     -> "FF0000";
            case MEDIUM   -> "FFA500";
            case LOW      -> "008000";
            case UNKNOWN  -> "808080";
        };
    }

    private String scoreColorHex(int score) {
        if (score <= 2) return "008000";   // green  — LOW range
        if (score <= 4) return "FFA500";   // orange — MEDIUM range
        if (score <= 6) return "FF0000";   // red    — HIGH range
        return "8B0000";                   // dark red — CRITICAL range (score 9)
    }

    private String indexColorHex(double index) {
        if (index >= 88) return "8B0000";
        if (index >= 63) return "FF0000";
        if (index >= 38) return "FFA500";
        return "008000";
    }

    private void addPageBreak(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setPageBreak(true);
    }

    private void setChartBackgroundWhite(XWPFChart chart) {
        CTChartSpace cs = chart.getCTChartSpace();
        CTShapeProperties spPr = cs.isSetSpPr() ? cs.getSpPr() : cs.addNewSpPr();
        setSolidFillRgb(spPr);

        CTPlotArea pa = chart.getCTChart().getPlotArea();
        CTShapeProperties plotSpPr = pa.isSetSpPr() ? pa.getSpPr() : pa.addNewSpPr();
        setSolidFillRgb(plotSpPr);
    }

    private String getSeverityDisplayName(Severity severity) {
        return switch (severity) {
            case CRITICAL -> "Crítico";
            case HIGH     -> "Alto";
            case MEDIUM   -> "Médio";
            case LOW      -> "Baixo";
            case UNKNOWN  -> "Desconhecido";
        };
    }

    private int getSeverityLevel(Severity severity) {
        return switch (severity) {
            case CRITICAL -> 4;
            case HIGH     -> 3;
            case MEDIUM   -> 2;
            case LOW      -> 1;
            case UNKNOWN  -> 0;
        };
    }

    private void setPieSliceRgb(XWPFChart chart, int pointIdx, int r, int g, int b) {
        CTPieChart pie = chart.getCTChart().getPlotArea().getPieChartArray(0);
        CTDPt dpt = pie.getSerArray(0).addNewDPt();
        dpt.addNewIdx().setVal(pointIdx);

        CTShapeProperties spPr = dpt.isSetSpPr() ? dpt.getSpPr() : dpt.addNewSpPr();
        CTSolidColorFillProperties solid = spPr.isSetSolidFill() ? spPr.getSolidFill() : spPr.addNewSolidFill();
        CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
        rgb.setVal(new byte[]{(byte) r, (byte) g, (byte) b});
        if (solid.isSetSrgbClr()) solid.setSrgbClr(rgb); else solid.addNewSrgbClr().set(rgb);
    }

    private void addAnswersTable(XWPFDocument document, Map<String, List<Answer>> answersByCategory,
                                 Map<String, Severity> severities, Map<Long, Question> questionMap) {
        final boolean[] first = {true};

        answersByCategory.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, List<Answer>>>comparingInt(e ->
                        getSeverityLevel(severities.getOrDefault(e.getKey(), Severity.UNKNOWN)))
                        .reversed())
                .forEach(entry -> {
                    if (!first[0]) {
                        document.createParagraph().setPageBreak(true);
                    }
                    first[0] = false;

                    String category = entry.getKey();
                    List<Answer> answers = entry.getValue();
                    Severity categorySeverity = severities.getOrDefault(category, Severity.UNKNOWN);

                    XWPFParagraph categoryHeader = document.createParagraph();
                    XWPFRun headerRun = categoryHeader.createRun();
                    int severityLevel = getSeverityLevel(categorySeverity);
                    String severityText = getSeverityDisplayName(categorySeverity);
                    headerRun.setText(severityLevel > 0
                            ? "Categoria: " + category + " - " + severityText + " (" + severityLevel + ")"
                            : "Categoria: " + category + " - " + severityText);
                    headerRun.setBold(true);
                    headerRun.setFontSize(14);
                    headerRun.setFontFamily("Calibri");
                    headerRun.setColor(severityColorHex(categorySeverity));

                    XWPFTable table = document.createTable();
                    XWPFTableRow header = table.getRow(0);
                    header.getCell(0).setText("Pergunta");
                    header.addNewTableCell().setText("Resposta");
                    header.addNewTableCell().setText("Tipo");
                    header.addNewTableCell().setText("Nível");
                    header.addNewTableCell().setText("Recomendação");

                    answers.stream()
                            .sorted(Comparator.comparing(
                                    a -> Optional.ofNullable(a.getChosenLevel()).orElse(OptionLevel.LOW),
                                    Comparator.reverseOrder()))
                            .forEach(answer -> {
                                XWPFTableRow row = table.createRow();
                                row.getCell(0).setText(answer.getQuestionText());
                                row.getCell(1).setText(answer.getUserResponse());
                                row.getCell(2).setText(answer.getQuestionType() != null ? answer.getQuestionType().name() : "-");

                                XWPFTableCell levelCell = row.getCell(3);
                                levelCell.setText(getOptionLevelDisplayName(answer.getChosenLevel()));
                                if (answer.getChosenLevel() != null) {
                                    levelCell.setColor(switch (answer.getChosenLevel()) {
                                        case HIGH   -> "FF0000";
                                        case MEDIUM -> "FFA500";
                                        case LOW    -> "008000";
                                    });
                                }

                                String recommendation = "-";
                                if (answer.getQuestionOptionId() != null) {
                                    Question q = questionMap.get(answer.getQuestionId());
                                    if (q != null) {
                                        recommendation = q.getOptions().stream()
                                                .filter(opt -> opt.getId().equals(answer.getQuestionOptionId()))
                                                .map(opt -> opt.getRecommendation() != null ? opt.getRecommendation() : "-")
                                                .findFirst()
                                                .orElse("-");
                                    }
                                }
                                row.getCell(4).setText(recommendation);
                            });

                    document.createParagraph();
                });
    }
}
